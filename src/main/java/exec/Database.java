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

    //Calculates nonce by grabbing number of transactions sent from an account and doing some crypto magic
    private BigInteger calculateNonce() throws ExecutionException, InterruptedException {
        EthGetTransactionCount ethGetTransactionCount = Gateway.web3.ethGetTransactionCount(
                sender, DefaultBlockParameterName.LATEST).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();

    }

    //Factoring out the transaction protocol. Requires created function (no need for encoding) to work. This automates
    //nonce calculatioin
    private EthSendTransaction createSendTransaction(Function function) throws ExecutionException, InterruptedException, IOException {
        this.isUnlocked();

        //Calculate nonce
        BigInteger nonce = calculateNonce();

        return createSendTransaction(function, nonce);

    }


    //Factors out transaction protocal, but this time only expects nonce passed as BigInteger.
    private EthSendTransaction createSendTransaction(Function function, BigInteger nonce) throws ExecutionException, InterruptedException, IOException {
        //Make sure we have privileges to account
        this.isUnlocked();

        //Encode function to bytecode to send
        String encodedFunction = FunctionEncoder.encode(function);

        //Create transaction (not send yet) using sender, nonce, and amount of gas requested and gas limit.
        //Geth/Parity signs the transaction with your account credentials here.
        Transaction transaction = Transaction.createFunctionCallTransaction(sender, nonce,
                new BigInteger("3000000", 10), new BigInteger("4000000", 10),
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
                Arrays.asList(),
                Arrays.asList(new TypeReference <Utf8String>() {
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
        Function function = new Function("addProperty", Arrays.asList(_propertyName),
                Collections.emptyList());

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

        Function function = new Function("addPropertyMetadata", Arrays.asList(_propertyName, _key, _value),
                Collections.emptyList());

        EthSendTransaction transactionResponse = createSendTransaction(function);

        System.out.println("Property metadata transaction hash: " + transactionResponse.getTransactionHash());

        return true;
    }

    //Call to return the metadata of specified property and key.
    public String getPropertyMetadata(String propertyName, String key) throws ExecutionException, InterruptedException {

        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _key = new Utf8String(key);

        Function function = new Function("getPropertyMetadata",
                Arrays.asList(_propertyName, _key),
                Arrays.asList(new TypeReference <Utf8String>() {
                }));

        List <Type> result = createSendCall(function);

        return result.get(0).getValue().toString();
    }

    public boolean addFile(String propertyName, String fileName) throws InterruptedException, ExecutionException, IOException {
        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _fileName = new Utf8String(fileName);

        Function function = new Function("addFile", Arrays.asList(_propertyName, _fileName),
                Collections.emptyList());


        EthSendTransaction transactionResponse = createSendTransaction(function);

        System.out.println("File creation transaction hash: " + transactionResponse.getTransactionHash());

        return true;
    }

    /*=================== FOR PROOF OF CONCEPT PURPOSES ONLY TO SEE IF WE CAN STORE ARRAYS IN CONTRACTS====================== */
    public boolean pushData(byte[] data) throws InterruptedException, ExecutionException, IOException {


        Bytes1[] castedData = new Bytes1[data.length];
        for (int i = 0; i < castedData.length; i++) castedData[i] = new Bytes1(new byte[]{data[i]});
        DynamicArray <Bytes1> _data = new DynamicArray <Bytes1>(castedData);
        Function function = new Function("uploadFile", Arrays.asList(_data),
                Collections.emptyList());

        EthSendTransaction transactionResponse = createSendTransaction(function);


        System.out.println("Upload size: " + castedData.length + "kB Upload hash: " + transactionResponse.getTransactionHash());
        System.out.println(transactionResponse.getError().getMessage());

        return true;
    }


    public byte[] pullData() throws ExecutionException, InterruptedException {

        Function function;
        Uint256 param0 = new Uint256(0);
        ArrayList <Byte> result = new ArrayList <Byte>();
        List <Type> out;


        while (true) {
            function = new Function("data",
                    Arrays.asList(param0),
                    Arrays.asList(new TypeReference <Bytes1>() {
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

    public boolean pushData(String propertyName, String fileName, byte[][] data) throws InterruptedException, ExecutionException, IOException {
        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _fileName = new Utf8String(fileName);
        Bytes32 _data;
        Uint256 count = new Uint256(0);
        EthSendTransaction transactionResponse;
        byte[] terminateSeq = new byte[]{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0};


        BigInteger nonce = calculateNonce();


        for (int btSeg = 0; btSeg < data.length; btSeg++) {
            count = new Uint256(btSeg);
            _data = new Bytes32(data[btSeg]);
            Function function = new Function("uploadFile", Arrays.asList(_propertyName,
                    _fileName, _data, count), Collections.emptyList());

            transactionResponse = createSendTransaction(function, nonce);
            nonce = nonce.add(BigInteger.ONE);
            System.out.println("Hash for " + fileName + " #" + btSeg + "/" + (data.length - 1) + ": "
                    + transactionResponse.getTransactionHash()
                    + " Current nonce: " + nonce.toString());
            if (transactionResponse.getTransactionHash() == null) {
                System.out.println("A problem occured in the upload process.\nError: "
                        + transactionResponse.getError().getMessage());
                return false;
            }
        }

        count = new Uint256(count.getValue().longValue() + 1);
        _data = new Bytes32(terminateSeq);
        Function function = new Function("uploadFile", Arrays.asList(_propertyName,
                _fileName, _data, count), Collections.emptyList());

        transactionResponse = createSendTransaction(function, nonce);
        System.out.println("Termination sequence hash: " + transactionResponse.getTransactionHash()
                + " Current nonce: " + nonce.toString());

        return true;
    }

    //Legacy pullData for old storage format. See pushData above.

    private boolean isTerminating(byte[] arr) {
        for (int i = 0; i < arr.length; i += 2) {
            if (!(arr[i] == 1 && arr[i + 1] == 0)) return false;
        }

        return true;
    }

    public byte[][] pullData(String propertyName, String fileName) throws ExecutionException, InterruptedException {

        Utf8String _propertyName = new Utf8String(propertyName);
        Utf8String _fileName = new Utf8String(fileName);

        Uint256 count = new Uint256(0);
        Function function;
        ArrayList <byte[]> result = new ArrayList <byte[]>();
        List <Type> out;

        while (true) {


            function = new Function("getFile",
                    Arrays.asList(_propertyName, _fileName, count),
                    Arrays.asList(new TypeReference <Bytes32>() {
                    }));

            out = createSendCall(function);

            if (out.size() == 0 || isTerminating(((Bytes32) out.get(0)).getValue())) break;

            result.add(((Bytes32) out.get(0)).getValue());
            System.out.println("Grabbed segment #" + count.getValue().toString());
            count = new Uint256(count.getValue().longValue() + 1);
        }

        byte[][] data = new byte[result.size()][32];

        for (int btSeg = 0; btSeg < data.length; btSeg++) {
            data[btSeg] = result.get(btSeg);
        }

        return data;
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
