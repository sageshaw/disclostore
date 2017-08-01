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

//This is the class that abstracts the transaction protocol. Note: I could have used a wrapped solidity file
//with web3j, but that means that I depend on having an updated contract on my computer to wrap and run it.
//This is why I manually create the transactions myself.

//Note: in order to submit convert java datatypes to solidity datatypes, we must wrap our parameters in web3j's
//wrappers to send. This is Utf8String for strings(wraps String), Uint256 for integers(wraps BigInteger), and
//Bytes1 for bytes(wraps byte arrays of length 1), and DynamicArray for arrays of unspecified length (wraps a list
//with desired Solidity wrapper type).

//Another note: to interact with the contract, we need the transaction/calls themselves are contained in Function objects.
//This need a string with the corresponding contract function name, an array of Solidity types to specify input parameters,
//and array of Solidity types (usually length one) for returned values.

public class Database {

    public static final String DATABASE_ID = "0.0.5";   //hard-coded version string to compare for correct database
    private String address;                             //address of contract
    private String sender;                              //address of etherbase account
    private String passkey;                             //password to etherbase account
    private PersonalUnlockAccount account;              //instance of accountUnlocker to make sure we have privelage to use account


    public Database(String contractAddress, String walletAddress, String password) {
        sender = walletAddress;
        address = contractAddress;
        passkey = password;

    }

    //Method to ensure we have access to account. Utilizes geth/parity methods to do this.
    private boolean isUnlocked() throws ExecutionException, InterruptedException {

        //If account unlocker has not been instantiated or account is locked, unlock the account with provided credentials
        if (account == null || !account.accountUnlocked()) {
            account = Gateway.web3.personalUnlockAccount(sender, passkey).sendAsync().get();
        }

        //report success or failure
        return account.accountUnlocked();
    }

    //Factoring out the transaction protocol. Requires created function (no need for encoding) to work.
    private EthSendTransaction createSendTransaction(Function function) throws ExecutionException, InterruptedException, IOException {
        //Make sure we have privileges to account
        this.isUnlocked();

        //Encode function to bytecode to send
        String encodedFunction = FunctionEncoder.encode(function);

        //Calculate nonce
        EthGetTransactionCount ethGetTransactionCount = Gateway.web3.ethGetTransactionCount(
                sender, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //Create transaction (not send yet) using sender, nonce, and amount of gas requested and gas limit.
        //Geth/Parity signs the transaction with your account credentials here.
        Transaction transaction = Transaction.createFunctionCallTransaction(sender, nonce,
                new BigInteger("800000", 10), new BigInteger("900000", 10),
                address, encodedFunction);

        //Send transaction and return the transaction response object.
        return Gateway.web3.ethSendTransaction(transaction).sendAsync().get();

    }

    //Factors out the call protocal. Requires created function (no encoding).
    private List<Type> createSendCall(Function function) throws ExecutionException, InterruptedException {
        //Encode function into bytecode.
        String encodedFunction = FunctionEncoder.encode(function);

        //Send call to blockchain.
        EthCall response = Gateway.web3.ethCall(
                Transaction.createEthCallTransaction(sender, address, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        //Decode response object to obtain list with returned parameters and return
        return FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
    }

    //Makes sure we are accessing correct contract (uses hardcoded version string for comparision)
    public boolean testAccess() throws ExecutionException, InterruptedException {
        //Create new function, includes return type
        Function function = new Function(
                "version",
                Arrays.<Type>asList(),
                Arrays. <TypeReference <?>>asList(new TypeReference <Utf8String>() {
                }));
        //Obtain returned list (length should be one, with Utf8String)
        List<Type> returned = createSendCall(function);

        //make sure we aren't accessing a wrong contract (not wrong version, but whole contract type)
        if (returned.size() == 0) return false;

        //make sure we are accessing the right contract version
        return returned.get(0).getValue().toString().equals(DATABASE_ID);
    }

    //Abstracts adding property function. Requires property name. This will allocate space on the blockchain
    //for a new property.
    //If called twice with same parameter, property is overwritten
    public boolean addProperty(String propertyName) throws ExecutionException, InterruptedException, IOException { //TODO: implement ensure account unlock security feature
        //Wrap for Solidity type conversion
        Utf8String _propertyName = new Utf8String(propertyName);

        //Create function
        Function function = new Function("addProperty", Arrays.<Type>asList(_propertyName),
                Collections.<TypeReference<?>>emptyList());

        //Send and obtain response object
        EthSendTransaction transactionResponse = createSendTransaction(function);

        //Print hash (for debugging purposes)
        System.out.println("Finished adding property '" + propertyName + "'\nTransaction Hash: "
                + transactionResponse.getTransactionHash());


        return true;

    }

    //Adds property metadata to already created property (nothing will happen if property does not exist.)
    //Requires strings key and data. Key maps to the data on contract.
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

    //Call to return the metadata of specified property and key.
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

    /*=================== FOR PROOF OF CONCEPT PURPOSES ONLY TO SEE IF WE CAN STORE ARRAYS IN CONTRACTS====================== */
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
    //Legacy pushData for old storage format when files were broken into 32 byte chunks and stored onto blockchain
    @Deprecated
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

    //Legacy pullData for old storage format. See pushData above.
    @Deprecated
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


    //Standard getters/setters
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
