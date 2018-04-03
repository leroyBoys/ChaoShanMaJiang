package com.rafo.chess.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *  系统命令调用
 */
public class Command {
    private final static String enterChar = "<br/>";
    private final static Command commd = new Command();
    private CommandInterFace commandInter;
    private Command(){
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")){
            commandInter = new CommandInterWindow();
        }else{
            commandInter = new CommandInterLinux();
        }
    }


    public static Command getInstance(){
        return commd;
    }

    /**
     *
     * @param file
     * @param findContent
     * @param statIndex(不包含)
     * @param endIndex(包含)
     * @return
     */
    public CommandReturnData getFindContent(String file,String findContent,int statIndex,int endIndex){
        //路径用"\\"
        return commandInter.getFindContent(file,findContent,statIndex,endIndex);
    }

    private CommandReturnData exec(String[] command){
        try {
            Process process = Runtime.getRuntime().exec(command);
            CommandReturnData commandResult = commandInter.wait(process);
            if (process != null) {
                process.destroy();
            }
            return commandResult;
        }catch (Exception e){
            e.printStackTrace();
            return new CommandReturnData(e.getMessage());
        }
    }

    private CommandReturnData exec(String command){
        try {
            Process process = Runtime.getRuntime().exec(command);
            CommandReturnData commandResult = commandInter.wait(process);
            if (process != null) {
                process.destroy();
            }
            return commandResult;
        }catch (Exception e){
            return new CommandReturnData(e.getMessage());
        }
    }

    public interface CommandInterFace{
        CommandReturnData getFindContent(String file, String findContent, int statIndex, int endIndex);
        CommandReturnData wait(Process process) throws InterruptedException, IOException;
    }

    public class CommandInterLinux implements CommandInterFace{
        public int DEFAULT_TIMEOUT = 5000;
        public static final int DEFAULT_INTERVAL = 1000;
        public long START;

        @Override
        public CommandReturnData getFindContent(String file,String findContent,int statIndex,int endIndex){
            CommandReturnData maxSizeData = Command.this.exec( new String[]{"/bin/sh","-c","cat "+file+" | grep "+findContent+" | wc -l"});
            int maxSize = Integer.valueOf(maxSizeData.content);
            if(statIndex >= maxSize){
                return new CommandReturnData("");
            }else if(endIndex >= maxSize){
                endIndex = maxSize;
            }
            String limitRequire = "";
            if(endIndex>0){
                limitRequire = " | head -n +"+(endIndex)+" | tail -n  "+(endIndex - statIndex);
            }

            return Command.this.exec( new String[]{"/bin/sh","-c","cat "+file+" | grep "+findContent+limitRequire});
        }

        private boolean isOverTime() {
            return System.currentTimeMillis() - START >= DEFAULT_TIMEOUT;
        }
        @Override
        public CommandReturnData wait(Process process) throws InterruptedException, IOException {
            BufferedReader errorStreamReader = null;
            BufferedReader inputStreamReader = null;
            try {
                errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                inputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                //timeout control
                START = System.currentTimeMillis();
                boolean isFinished = false;
                for (;;) {
                    if (isOverTime()) {
                        return new CommandReturnData("Command process timeout");
                    }
                    if (isFinished) {
                        process.waitFor();
                        //parse error info
                        if (errorStreamReader.ready()) {
                            StringBuilder buffer = new StringBuilder();
                            String line;
                            while ((line = errorStreamReader.readLine()) != null) {
                                buffer.append(line+enterChar);
                            }
                            return new CommandReturnData(buffer.toString());
                        }
                        //parse info
                        if (inputStreamReader.ready()) {
                            StringBuilder buffer = new StringBuilder();
                            String line;
                            int count = 0;
                            while ((line = inputStreamReader.readLine()) != null) {
                                buffer.append(line);
                                count++;
                            }
                            return new CommandReturnData(buffer.toString(),count);
                        }
                        return null;
                    }
                    try {
                        isFinished = true;
                  //      process.exitValue();
                    } catch (IllegalThreadStateException e) {
                        // process hasn't finished yet
                        isFinished = false;
                        Thread.sleep(DEFAULT_INTERVAL);
                    }
                }
            } finally {
                if (errorStreamReader != null) {
                    try {
                        errorStreamReader.close();
                    } catch (IOException e) {
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public class CommandInterWindow implements CommandInterFace{
        @Override
        public CommandReturnData getFindContent(String file,String findContent,int statIndex,int endIndex){
           // return Command.this.exec("cmd /c findstr "+findContent+" "+file);
            String newFile = file+System.currentTimeMillis();
            try{
                String cmd = "cmd /c  findstr "+findContent+" "+ file+" >"+newFile;
                Command.this.exec(cmd);
                if(statIndex == 0){
                    cmd = "cmd /c for /f \"tokens=1* delims=:\" %i in ('findstr/n .* "+newFile+" ')do @if %i leq "+endIndex+" echo.%j ";
                }else{
                    cmd = "cmd /c for /f \"skip="+statIndex+" tokens=1* delims=:\" %i in ('findstr/n .* "+newFile+" ')do @if %i leq "+endIndex+" echo.%j ";
                }

                CommandReturnData returnData =  Command.this.exec(cmd);
                return returnData;
            }catch (Exception e){
                return new CommandReturnData(e.getMessage());
            }finally {
                File f = new File(newFile);
                if(f.exists()){
                    new File(newFile).delete();
                }
            }
        }

        @Override
        public CommandReturnData wait(Process process) throws InterruptedException, IOException {
            try {
                BufferedReader strCon = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                int count = 0;
                while ((line = strCon.readLine()) != null) {
                    sb.append(line+enterChar);
                    count++;
                }

                return new CommandReturnData(sb.toString(),count);
            }catch (Exception e){
            }finally {
            }
            return null;
        }
    }
    public static class CommandReturnData{
        private String content;
        private int count;

        public CommandReturnData(String content,int count){
            this.content = content;
            this.count = count;
        }
        public CommandReturnData(String content){
            this.content = content;
        }
        public String getContent(){
            return content;
        }
        public int getCount(){
            return count;
        }
    }

}