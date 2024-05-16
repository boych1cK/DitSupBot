package dit.group.DitSupBot.LDAP;


import lombok.extern.slf4j.Slf4j;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;

@Slf4j
public class LDAP {
    Hashtable<String, Object> env = new Hashtable<>();
    InitialLdapContext context;

    public LDAP(){
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://10.0.6.200:389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "notification@oksshs.local");
        env.put(Context.SECURITY_CREDENTIALS, "");
        try {
            context = new InitialLdapContext(env,null);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(context);
    }
    public String getName(String Mail){
        String name = null;
        try {
            String searchFilter ="(mail="+Mail+")";
            String[] reqAtt ={"cn","mail"};
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(reqAtt);
            NamingEnumeration users = context.search("OU=_Users,DC=oksshs,DC=local",searchFilter, controls );
            SearchResult result = null;
            while(users.hasMore()){
                result = (SearchResult) users.next();
                Attributes attributes = result.getAttributes();
                //System.out.println(attributes);
                name = String.valueOf(attributes.get("cn"));
                name=name.substring(4);

            }
        }catch (NamingException e)
        {
            name = "error";
            e.printStackTrace();
        }
        return  name;
    }
    public Boolean MailVerify(String Mail){
        boolean flag = false;
        try {
            String searchFilter ="(mail="+Mail+")";
            String[] reqAtt ={"cn"};
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(reqAtt);
            NamingEnumeration users = context.search("OU=_Users,DC=oksshs,DC=local",searchFilter, controls );
            SearchResult result = null;
            while(users.hasMore()){
                result = (SearchResult) users.next();
                Attributes attributes = result.getAttributes();
                //System.out.println(attributes.get("cn"));
                flag = true;
                log.info("Осуществлен вход: " + String.valueOf(attributes.get("cn")).substring(4) );

            }
        }catch (NamingException e)
        {
            e.printStackTrace();
        }
        try {
            context.close();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return flag;


    }



}
