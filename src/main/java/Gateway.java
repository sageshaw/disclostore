import java.io.IOException;
import java.math.BigInteger;


import com.github.lalyos.jfiglet.FigletFont;

import org.web3j.crypto.CipherException;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import solids.Storage;

//wallet passcode is 'manju'
public class Gateway {



    public static void main(String[] args) throws IOException, CipherException {


        Commander cmdHandle;
        Storage stor = null;

        String asciiTitle = FigletFont.convertOneLine("Disclostore");
        System.out.println(asciiTitle);
        System.out.println("Version Alpha 0.1\n");

        Web3j web3 = null;
        Web3ClientVersion web3ClientVersion = null;
        String clientVersion;

        Credentials credentials = null;

        try {
             web3 = Web3j.build(new HttpService("http://192.168.250.33:8545")); //TODO: Allow user to specify RPC IP and Port (currently defaults to localhost:8545"
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
        }

        try {
            credentials =  WalletUtils.loadCredentials("manju", "C:/Users/seiji/devstuff/devchain/keystore/UTC--2017-07-25T21-58-33.423183300Z--cf556eb7a1aedb38b9252d32afeef61a44edd08b");
        } catch (Exception e) {
            System.out.println("Bugger. Can't access your wallet. Make sure you have the correct path and login credentials.");
            e.printStackTrace();
            System.exit(3);
        }

        try {
            stor = Storage.load("0xc29002e61737Ab3337d365Db67200D09f141684f", web3, credentials,
                    new BigInteger("999999", 10), new BigInteger("9999999", 10));
        } catch (Exception e) {
            System.out.println("Couldn't find database! Make sure address is correct.");
            e.printStackTrace();
        } finally {

            if (!stor.isValid()) {
                System.out.println("Database not valid.");


            }
        }


        assert web3ClientVersion != null;
        assert credentials != null;
        assert web3 != null;
        assert stor.isValid() == true;

        System.out.println("\nHuzzah! You're connected. Here's your Ethereum client: " + clientVersion);
        System.out.println("Current wallet address: " + credentials.getAddress());
        System.out.println("Connected to database: " + stor.isValid() + "\n");



        cmdHandle = new Commander();
        cmdHandle.parseCommand(args);



    }
}
