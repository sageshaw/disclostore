package exec;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Database {

    private String address;
    private String sender;
    private String passkey;

    public static final String DATABASE_ID = "300";
    private PersonalUnlockAccount account;


    public Database(String contractAddress, String walletAddress, String password) {
        sender = walletAddress;
        address = contractAddress;
        passkey = password;
    }

    private boolean isUnlocked() throws ExecutionException, InterruptedException {
        if (account == null || !account.accountUnlocked()) {
            account = Gateway.web3.personalUnlockAccount(sender, passkey).sendAsync().get();
        }

        return account.accountUnlocked();
    }

    private EthSendTransaction createSendTransaction(Function function) throws ExecutionException, InterruptedException {

        System.out.println("Account credentials loaded: " + isUnlocked());

        String encodedFunction = FunctionEncoder.encode(function);

        EthGetTransactionCount ethGetTransactionCount = Gateway.web3.ethGetTransactionCount(
                sender, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        Transaction transaction = Transaction.createFunctionCallTransaction(sender, nonce,
                new BigInteger("200000", 10), new BigInteger("900000", 10),
                address, encodedFunction);

        return Gateway.web3.ethSendTransaction(transaction).sendAsync().get();
    }

    private List<Type> createSendCall(Function function) throws ExecutionException, InterruptedException {
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = Gateway.web3.ethCall(
                Transaction.createEthCallTransaction(sender, address, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();
        return FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
    }


    public boolean testAccess() throws ExecutionException, InterruptedException {

        Function function = new Function(
                "verificationID",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));

        List<Type> returned = createSendCall(function);

        return returned.get(0).getValue().toString().equals(DATABASE_ID);
    }

    public boolean addProperty(String propertyname) throws ExecutionException, InterruptedException { //TODO: implement ensure account unlock security feature

        Utf8String _propertyName = new Utf8String(propertyname);
        Function function = new Function("addProperty", Arrays.<Type>asList(_propertyName),
                Collections.<TypeReference<?>>emptyList());

        EthSendTransaction transactionResponse = createSendTransaction(function);

        System.out.println("Transaction hash: " + transactionResponse.getTransactionHash());

        return true;


    }

    public void setAddress(String contractAddress) {
        address = contractAddress;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getAddress() {
        return address;
    }

    public String getSender() {
        return sender;
    }
}
