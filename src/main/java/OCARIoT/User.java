package OCARIoT;

public class User {

    int id;
    String token;

    public User (int id, String token){
        this.id = id;
        this.token=token;

    }

    public int getId(){
        return id;
    }


    public void setId(){

        this.id=id;
    }

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }

}