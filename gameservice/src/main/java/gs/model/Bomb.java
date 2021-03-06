package gs.model;

import gs.geometry.Point;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Bomb extends GameObject implements Tickable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Bomb.class);
    public static final int DEFAULT_CARRIED_BOMB_LIFETIME = 10000;
    public static final int DEFAULT_PLANTED_BOMB_LIFETIME = 2000;
    private static final int BOMB_WIDTH = 28;
    private static final int BOMB_HEIGHT = 28;
    private int lifetime;
    private transient Player owner;
    private transient int elapsed = 0;

    public Bomb(GameSession session, Point position, Player owner, int lifetime) {
        super(session, new Point(position.getX(), position.getY()),
                "Bomb", BOMB_WIDTH, BOMB_HEIGHT);
        this.owner = owner;
        this.lifetime = lifetime;
        session.addBomb(this);
    }

    public List<Point> getBlast() {
        int range = owner.getBombRange();
        int i = 1;
        List<Point> blasts = new ArrayList<>();
        blasts.add(position);
        while(i <= range) {
            blasts.add(new Point(position.getX() + GameObject.getHeightBox() * i, position.getY()));
            blasts.add(new Point(position.getX() - GameObject.getHeightBox() * i, position.getY()));
            blasts.add(new Point(position.getX(), position.getY() + GameObject.getHeightBox() * i));
            blasts.add(new Point(position.getX(), position.getY() - GameObject.getHeightBox() * i));
            i++;
        }
        return blasts;
    }

    public Player getOwner() {
        return this.owner;
    }

    @Override
    public void tick(int elapsed) {
        this.elapsed += elapsed;
    }

    public boolean dead() {
        return this.elapsed >= lifetime;
    }

    public void reset(){
        this.elapsed = 0;
    }
}
