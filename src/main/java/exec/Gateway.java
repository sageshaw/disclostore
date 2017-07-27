package exec;

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

    public static Parity web3;
    public static Database storage;
    public static Credentials credentials;

    public static void main(String[] args) throws IOException, CipherException {


        Commander cmdHandle;


        String asciiTitle = FigletFont.convertOneLine("Disclostore");
        System.out.println(asciiTitle);
        System.out.println("Version Alpha 0.1\n");


        Web3ClientVersion web3ClientVersion = null;
        String clientVersion;



        try {
            web3 = Parity.build(new HttpService("http://192.168.250.40:8545")); //TODO: Allow user to specify RPC IP and Port (currently defaults to localhost:8545"
        } catch (Exception e) {
            System.out.println("Could not connect to specified RPC port.");
            e.printStackTrace();
            System.exit(1);
        }

        try {
             web3ClientVersion = web3.web3ClientVersion().send();
        } catch (Exception e){
            System.out.println("Could not connect to Ethereum client. Please make sure your client is running.");
            e.printStackTrace();
            System.exit(2);
        } finally {
            clientVersion = web3ClientVersion.getWeb3ClientVersion();
            System.out.println("\nHuzzah! You're connected. Here's your Ethereum client: " + clientVersion);
        }

        try {
            credentials =  WalletUtils.loadCredentials("manju", "C:/Users/seiji/devstuff/devchain/keystore/UTC--2017-07-25T21-58-33.423183300Z--cf556eb7a1aedb38b9252d32afeef61a44edd08b");
        } catch (Exception e) {
            System.out.println("Bugger. Can't access your wallet. Make sure you have the correct path and login credentials.");
            e.printStackTrace();
            System.exit(3);
        } finally {
            System.out.println("Current wallet address: " + credentials.getAddress());
        }

        storage = new Database("0x40D08129aDEDd391c203900B6e785539cCC38785", credentials.getAddress(), "manju");
        int testInt = -1;

        try {
            System.out.println("Accessing correct database: " + storage.testAccess());

        } catch (Exception e) {
            System.out.println("Couldn't find database! Make sure address is correct.");
            e.printStackTrace();
            System.exit(4);
        }

        System.out.println();

        assert web3ClientVersion != null;
        assert credentials != null;
        assert web3 != null;


        cmdHandle = new Commander();
        cmdHandle.parseCommand(args);


    }
}
