package com.freeteam01.androidnewsgroupreader.Models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Author {
    private String forname;
    private String surname;
    private String name_string;
    private String e_mail;
    private String from_input_string;

    private boolean only_alias;

    public Author(String from){
        from_input_string = from;
        only_alias = false;
        splitAndSetAuthorInfo();
    }

    public String getSurname(){
        return surname;
    }

    public String getNameString(){
        return name_string;
    }

    public String getEmail(){
        return e_mail;
    }


    private void splitAndSetAuthorInfo(){
        Pattern p = Pattern.compile("[^(<)]*");
        Matcher m = p.matcher(from_input_string);
        if(m.find()) {
            name_string = m.group(0).trim();
            if(name_string.contains(" ")) {
                forname = name_string.substring(0, name_string.indexOf(" ")-1);
                surname = name_string.substring(name_string.indexOf(" ")+1, name_string.length()-1);
            }
            else
                only_alias = true;
        }
        else
            only_alias = true;
        e_mail = from_input_string.replace(m.group(0), "").replace("<", "").replace(">", "");
    }

}
