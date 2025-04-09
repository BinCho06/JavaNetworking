public class Player {
    private int x, y;
    public boolean up, down, left, right;
    private String username;
    private String uuid;
    
    public Player(String uuid){
        this.uuid = uuid;
        x = (int)(Math.random()*20);
        y = (int)(Math.random()*20);
    }

    public Player(String username, int x, int y){
        this.x = x;
        this.y = y;
        this.username = username;
    }

    public void update(){
        move();
    }

    private void move(){
        if(up) y--;
        if(down) y++;
        if(left) x--;
        if(right) x++;
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public String getUUID(){
        return uuid;
    }
    public String getUsername(){
        return username;
    }
    public String getPlayer(){
        return username+":"+x+":"+y;
    }

    public void setUsername(String username){
        this.username = username;
    }
}