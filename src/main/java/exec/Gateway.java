package exec;

/*Main class. This is what collects user input and guides program through regular workflow.
This also holds objects that need to be accessed by many other classes. Note: if this ever is
multi-threaded, use Singleton pattern for database.
*/

import com.github.lalyos.jfiglet.FigletFont;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;

import java.io.IOException;


//wallet passcode is 'manju'
public class Gateway {


    //Instanced that need to be accessible to all other classes
    public static Parity web3;              //instance of web3j used to communicate with Ethereum client (must be geth OR parity)
    public static Database storage;         //instance of storage (abstracts transaction protocals even further from what web3j does)
    public static Credentials credentials;  //instance of user wallet to supply ether for transactions

    public static void main(String[] args) throws IOException, CipherException {

        //Parses input and executes commands. Note: this program uses a half-baked command pattern to run.
        Commander cmdHandle;

        //Some fun ascii art For a nice title.
        String asciiTitle = FigletFont.convertOneLine("Disclostore");
        System.out.println(asciiTitle);
        System.out.println("Version Alpha 0.1\n");

        //Used for user information.
        Web3ClientVersion web3ClientVersion = null;
        String clientVersion;


        //TODO: make config file for user-specified ports and wallet locations (XML or JSON)
        //Establish connection with ethereum client via RPC. IPC is available, but this is more flexible.
        try {
            web3 = Parity.build(new HttpService("http://192.168.250.39:8545")); //defaults to 'http://localhost:8545'
        } catch (Exception e) {
            System.out.println("Could not connect to specified RPC port.");
            e.printStackTrace();
            System.exit(1);
        }

        //Grab version information from ethereum client. Good litmus test for an established connection for data transfer
        try {
            web3ClientVersion = web3.web3ClientVersion().send();
        } catch (Exception e) {
            System.out.println("Could not connect to Ethereum client. Please make sure your client is running.");
            e.printStackTrace();
            System.exit(2);
        } finally {
            clientVersion = web3ClientVersion.getWeb3ClientVersion();
            System.out.println("\nHuzzah! You're connected. Here's your Ethereum client: " + clientVersion);
        }

        //Load wallet utilities
        try {
            credentials = WalletUtils.loadCredentials("manju", "C:/Users/seiji/devstuff/devchain/keystore/UTC--2017-07-25T21-58-33.423183300Z--cf556eb7a1aedb38b9252d32afeef61a44edd08b");
        } catch (Exception e) {
            System.out.println("Bugger. Can't access your wallet. Make sure you have the correct path and login credentials.");
            e.printStackTrace();
            System.exit(3);
        } finally {
            System.out.println("Current wallet address: " + credentials.getAddress());
        }

        //Instantiate database with provided contract address
        storage = new Database("0xBe075A4d4DF77D9A2d903b7031E4b318fEBFda09", credentials.getAddress(), "manju");

        //Try to access and check version number (hardcoded into database). Good litmus test for blockchain connection.
        try {
            System.out.println("Accessing correct database: " + storage.testAccess());

        } catch (Exception e) {
            System.out.println("Couldn't find database! Make sure address is correct.");
            e.printStackTrace();
            System.exit(4);
        }

        System.out.println();

        //If all of those try's execute completely, these should be true:
        assert web3ClientVersion != null;
        assert credentials != null;
        assert web3 != null;

        //Instantiate commander and parse provided arguments for action
        cmdHandle = new Commander();
        cmdHandle.parseCommand(args);

        //Just to let user know that program is done executing all of instruction (may take a while to close RPC port
        System.out.println("\nExiting...");

    }
}
