package exec;


import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes1;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Database {

    public static final String DATABASE_ID = "0.0.5";
    private String address;
    private String sender;
    private String passkey;
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


    private EthSendTransaction createSendTransaction(Function function) throws ExecutionException, InterruptedException, IOException {

        this.isUnlocked();

        String encodedFunction = FunctionEncoder.encode(function);

        EthGetTransactionCount ethGetTransactionCount = Gateway.web3.ethGetTransactionCount(
                sender, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        Transaction transaction = Transaction.createFunctionCallTransaction(sender, nonce,
                new BigInteger("800000", 10), new BigInteger("900000", 10),
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
                "version",
                Arrays.<Type>asList(),
                Arrays. <TypeReference <?>>asList(new TypeReference <Utf8String>() {
                }));

        List<Type> returned = createSendCall(function);

        if (returned.size() == 0) return false;

        return returned.get(0).getValue().toString().equals(DATABASE_ID);
    }

    public boolean addProperty(String propertyName) throws ExecutionException, InterruptedException, IOException { //TODO: implement ensure account unlock security feature

        Utf8String _propertyName = new Utf8String(propertyName);
        Function function = new Function("addProperty", Arrays.<Type>asList(_propertyName),
                Collections.<TypeReference<?>>emptyList());

        EthSendTransaction transactionResponse = createSendTransaction(function);

        System.out.println("Finished adding property '" + propertyName + "'\nTransaction Hash: "
                + transactionResponse.getTransactionHash());


        return true;

    }

    public boolean addPropertyMetadata(String propertyName, String key, String value) throws ExecutionException, InterruptedException, IOException {

        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _key = new Utf8String(key);
        Utf8String _value = new Utf8String(value);

        Function function = new Function("addPropertyMetadata", Arrays.<Type>asList(_propertyName, _key, _value),
                Collections.<TypeReference<?>>emptyList());

        EthSendTransaction transactionResponse = createSendTransaction(function);

        System.out.println("Property metadata transaction hash: " + transactionResponse.getTransactionHash());

        return true;
    }

    public String getPropertyMetadata(String propertyName, String key) throws ExecutionException, InterruptedException {

        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _key = new Utf8String(key);

        Function function = new Function("getPropertyMetadata",
                Arrays. <Type>asList(_propertyName, _key),
                Arrays. <TypeReference <?>>asList(new TypeReference <Utf8String>() {
                }));

        List <Type> result = createSendCall(function);

        return result.get(0).getValue().toString();
    }

    /*=================== FOR TESTING PURPOSES ONLY TO SEE IF WE CAN STORE ARRAYS IN CONTRACTS====================== */
    public boolean pushData(byte[] data) throws InterruptedException, ExecutionException, IOException {


        Bytes1[] castedData = new Bytes1[data.length];
        for (int i = 0; i < castedData.length; i++) castedData[i] = new Bytes1(new byte[]{data[i]});
        DynamicArray <Bytes1> _data = new DynamicArray <Bytes1>(castedData);
        Function function = new Function("uploadFile", Arrays. <Type>asList(_data),
                Collections. <TypeReference <?>>emptyList());

        EthSendTransaction transactionResponse = createSendTransaction(function);

        System.out.print("Upload hash: " + transactionResponse.getTransactionHash());

        return true;
    }

    public byte[] pullData() throws ExecutionException, InterruptedException {

        Function function;
        Uint256 param0 = new Uint256(0);
        ArrayList <Byte> result = new ArrayList <Byte>();
        List <Type> out;


        while (true) {
            function = new Function("data",
                    Arrays. <Type>asList(param0),
                    Arrays. <TypeReference <?>>asList(new TypeReference <Bytes1>() {
                    }));
            out = createSendCall(function);

            if (out.size() == 0) break;

            result.add(((Bytes1) out.get(0)).getValue()[0]);

            param0 = new Uint256(param0.getValue().longValue() + 1);

        }


        byte[] data = new byte[result.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = result.get(i).byteValue();
        }

        return data;
    }


    /*=============================== END OF TESTING ZONE=========================================================== */
    public boolean pushData(String propertyName, String fileName, byte[] data, int index) throws InterruptedException, ExecutionException, IOException {
        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _fileName = new Utf8String(fileName);
        Bytes32 _data = new Bytes32(data);
        Uint256 count = new Uint256(index);

        Function function = new Function("uploadFile", Arrays. <Type>asList(_propertyName,
                _fileName, _data, count), Collections. <TypeReference <?>>emptyList());

        EthSendTransaction transactionResponse = createSendTransaction(function);

        System.out.println("Hash for " + fileName + " #" + index + ": " + transactionResponse.getTransactionHash());

        return true;
    }

    public byte[] pullData(String propertyName, String fileName, int index) throws ExecutionException, InterruptedException {

        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _fileName = new Utf8String(fileName);
        Uint256 count = new Uint256(index);

        Function function = new Function("getFile",
                Arrays. <Type>asList(_propertyName, _fileName, count),
                Arrays. <TypeReference <?>>asList(new TypeReference <Bytes32>() {
                }));

        List <Type> returned = createSendCall(function);

        return (byte[]) returned.get(0).getValue(); //TODO test to make sure this works
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String contractAddress) {
        address = contractAddress;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
