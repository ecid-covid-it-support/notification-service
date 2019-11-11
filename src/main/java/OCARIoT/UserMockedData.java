package OCARIoT;

import java.util.ArrayList;
import java.util.List;

public class UserMockedData {

    //list of users
    private List<User> users;

    private static UserMockedData instance = null;
    public static UserMockedData getInstance(){
        if(instance == null){
            instance = new UserMockedData();
        }
        return instance;
    }


    public UserMockedData(){
        users  = new ArrayList<User>();
        users.add(new User(1, "token1"));
        users.add(new User(2, "token2"));
        users.add(new User(3, "token3"));
        users.add(new User(4, "token4"));
        users.add(new User(5, "token5"));
    }

    // return all users
    public List<User> fetchUsers() {
        return users;
    }

    // return user by id
    public User getUserById(int id) {
        for(User b: users) {
            if(b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    //return token by id
    public String getTokenById(int id) {
        for(User b: users) {
            if(b.getId() == id) {
                return b.getToken();
            }
        }
        return null;
    }


    // search user by token
    public List<User> searchUsers(String searchTerm) {
        List<User> searchedUsers = new ArrayList<User>();
        for(User b: users) {
            if(b.getToken().contains(searchTerm)){
                searchedUsers.add(b);
            }
        }
        return searchedUsers;
    }

    // create user
    public User createUser(int id, String token) {
        User newUser = new User(id, token);
        users.add(newUser);
        return newUser;
    }

    // update User
    public User updateUser(int id, String token) {
        for(User b: users) {
            if(b.getId() == id) {
                b.setToken(token);
                return b;
            }

        }

        return null;
    }

    // delete User by id
    public boolean deleteUser(int id){
        int blogIndex = -1;
        for(User b: users) {
            if(b.getId() == id) {
                blogIndex = users.indexOf(b);
                continue;
            }
        }
        if(blogIndex > -1){
            users.remove(blogIndex);
        }
        return true;
    }

}
