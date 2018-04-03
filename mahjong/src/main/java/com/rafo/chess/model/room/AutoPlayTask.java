package com.rafo.chess.model.room;

import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.majiang.service.MJGameService;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.utils.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Set;

/**
 * 自动打牌任务
 */
public class AutoPlayTask implements Runnable {

    private GameExtension extension;
    private volatile boolean isRun;
    private final Logger logger = LoggerFactory.getLogger("task");

    public AutoPlayTask(GameExtension extension) {
        this.extension = extension;
    }

    @Override
    public void run() {
        try {
            if(isRun){
                return;
            }
            isRun = false;

            Set<GameRoom> rooms = RoomManager.getAllRooms();
            for(GameRoom gameRoom : rooms){
                gameRoom.tick(true);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.debug("Room Info error!!!!", e);
        }
    }


}
