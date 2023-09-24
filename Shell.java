import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Shell 
{
    public static void main(String[] args)
    {
        System.out.println(Shell.command("cd", ".").toString());
    }

    public static ArrayList<String> command (final String cmdLine, final String directory)
    {
        try
        {
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");
            String[] args;
            args = isWindows ? new String[] {"cmd.exe", "/c", cmdLine} : 
                                new String[] {"bash", "-c", cmdLine};

            Process process = new ProcessBuilder(args)
                            .redirectErrorStream(true)
                            .directory(new File(directory))
                            .start();
            ArrayList<String> output = new ArrayList<String>();
            BufferedReader br = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null )
                output.add(line);
            
            //There should really be a timeout here.
            if (0 != process.waitFor())
                return null;

            return output;
        }
        catch(Exception e)
        {
            //Warning: doing this is no good in high quality applications.
            //Instead, present appropriate error messages to the user.
            //But it's perfectly fine for prototyping.
            System.out.println(e);
            return null;
        }
    }
}
