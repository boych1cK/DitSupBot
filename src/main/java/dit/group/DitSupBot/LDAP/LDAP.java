package dit.group.DitSupBot.LDAP;


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


public class LDAP {
    Hashtable<String, Object> env = new Hashtable<>();
    InitialLdapContext context;

    public LDAP(){
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://10.0.7.231:389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "noreply@nshmu6.com");
        env.put(Context.SECURITY_CREDENTIALS, "63UjkHz,JreJcm");
        try {
            context = new InitialLdapContext(env,null);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        System.out.println(context);
    }
    public Boolean MailVerify(String Mail){
        boolean flag = false;
        try {
            String searchFilter ="(mail="+Mail+")";
            String[] reqAtt ={""};
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(reqAtt);
            NamingEnumeration users = context.search("OU=_Users,DC=nshmu6,DC=com",searchFilter, controls );
            SearchResult result = null;
            while(users.hasMore()){
                result = (SearchResult) users.next();
                Attributes attributes = result.getAttributes();
                System.out.println(attributes.get("cn"));
                flag = true;
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
