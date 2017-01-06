package utility.exceptions;

/**
 * Created by Sermilion on 13/09/16.
 */
public class NoArticleForTagException extends IllegalArgumentException {

    public NoArticleForTagException(String message){
        super(message);
    }
}
