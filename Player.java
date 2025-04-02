public class Player {
    private int x, y;
    public boolean up, down, left, right;
    
    public Player(){
        x = (int)(Math.random()*21)-10;
        y = (int)(Math.random()*21)-10;
    }

    public void move(){
        if(up) y--;
        if(down) y++;
        if(left) x--;
        if(right) x++;
    }

    public String toString(){
        return "Position x:"+x+" y:"+y;
    }
}