package it.marcoreni.nexus3.bitbucketcloud.auth.plugin;

public class BitbucketAuthenticationException extends Exception{
    public BitbucketAuthenticationException(String message){
        super(message);
    }

    public BitbucketAuthenticationException(Throwable cause){
        super(cause);
    }

}
