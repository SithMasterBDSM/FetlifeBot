package sithmaster;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.StringTokenizer;

@Getter
@Setter
@ToString
public class UserTypeInfo {
    private int age;
    private String genreHints;
    private String role;

    UserTypeInfo(String hint) {
        StringBuilder headNumber = new StringBuilder();
        int i;
        for ( i = 0; i < hint.length(); i++ ) {
            char c = hint.charAt(i);
            if ( Character.isDigit(c) ) {
                headNumber.append(c);
            } else {
                break;
            }
        }

        String tail = hint.substring(i);

        this.age = Integer.parseInt(headNumber.toString());
        this.genreHints = "";
        this.role = "[Not set]";

        StringTokenizer parser = new StringTokenizer(tail, " ");
        for ( i = 0; parser.hasMoreTokens(); i++ ) {
            String token = parser.nextToken();
            switch ( i ) {
                case 0:
                    this.genreHints = token;
                    break;
                case 1:
                    this.role = token;
                    break;
                default:
                    this.role += "-" + token + "(WARNING)";
                    break;
            }
        }
    }
}
