package com.rafo.chess.handlers.admin;

import com.rafo.chess.model.GateRequest;
import com.rafo.chess.utils.Command;
import com.rafo.chess.utils.DateTimeUtil;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;

import java.io.File;
import java.util.Date;

/**
 * Created by Administrator on 2017/1/10.
 */
public class GMLogHandler  extends GMHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject, SFSExtension roomExt) {
/*        isfsObject = isfsObject.getSFSObject("params");
        if(isfsObject == null){
            AdminCmd.dealUser(user,"");
            AdminCmd.getInstance().sendChat(roomExt,user,"params error");
            return;
        }
        GateRequest request = new GateRequest(user);

        String cmd = isfsObject.getUtfString("admin_cmd_from_gm");
        String logName = isfsObject.getUtfString("log_name");
        String date = isfsObject.getUtfString("log_date");
        String log_get = isfsObject.getUtfString("log_get");
        int pageNum = isfsObject.getInt("log_pageSize");
        AdminCmd.dealUser(user,cmd);

        String logPath = getLogFileDictoryPath(logName);
        if(logPath == null){
            AdminCmd.getInstance().sendChat(roomExt,user,logName+" not eixt!");
            return;
        }
        if(log_get != null && log_get.equals("1")){
            AdminCmd.getInstance().sendChat(roomExt,user,logPath);
            return;
        }

        Command command = Command.getInstance();

        final int pageSize = 2;
        int startIndx = pageSize*pageNum;
        int endIndex = pageSize*(pageNum+1);

        Command.CommandReturnData commandReturnData;
        if(date != null && !date.isEmpty() && !DateTimeUtil.getDateFormat(new Date(),"yyyyMMdd").equals(date)){
            commandReturnData = command.getFindContent(logPath+"."+date,cmd,startIndx,endIndex);
        }else{
            commandReturnData = command.getFindContent(logPath,cmd,startIndx,endIndex);
        }

        String str = "";
        if(commandReturnData != null ){
            str = commandReturnData.getContent();
            if(commandReturnData.getCount() ==pageSize){
                pageNum++;
            }
        }

        AdminCmd.getInstance().sendChat(roomExt,user,str,String.valueOf(pageNum));*/
    }


    private String getLogFileDictoryPath(String logName){
        org.apache.log4j.Logger rootLogger = LogManager.getLogger(logName);
        if(rootLogger != null){

            FileAppender appender = (FileAppender)rootLogger.getAppender(logName);
            if(appender != null){

                File f = new File(appender.getFile());
                if(!f.exists()){
                    return null;
                }
                //System.out.println(f.getAbsolutePath()+"-1-->"+f.getParentFile().getAbsolutePath());
                return f.getAbsolutePath();
            }
        }
        return null;
    }
}
