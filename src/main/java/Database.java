import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Database {

    private String address;
    private String sender;
    public static final String DATABASE_ID = "3";

    public Database(String contractAddress, String walletAddress) {
        sender = walletAddress;
        address = contractAddress;
    }

    public boolean testAccess() throws ExecutionException, InterruptedException {
        Function testAccess = new Function (
                "testInt",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        String encodedTestAccess = FunctionEncoder.encode(testAccess);
        org.web3j.protocol.core.methods.response.EthCall response = Gateway.web3.ethCall(
                Transaction.createEthCallTransaction(sender, address, encodedTestAccess),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();
        List<Type> types = FunctionReturnDecoder.decode(response.getValue(), testAccess.getOutputParameters());

        return types.get(0).getValue().toString().equals(DATABASE_ID);
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
