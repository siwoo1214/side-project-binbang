package com.binbang.backend.chat.exception;

/**
 * 채팅방을 찾을 수 없을 때 나오는 예외
 */
public class ChatRoomNotFoundException extends RuntimeException{

    public ChatRoomNotFoundException(Long chatRoomId){
        super("채팅방을 찾을 수 없습니다: "+chatRoomId);
    }

    public ChatRoomNotFoundException(String message){
        super(message);
    }

}
