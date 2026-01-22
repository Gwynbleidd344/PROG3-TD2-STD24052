import java.time.Instant;

public class StockMovement {
    private int id;
    private StockValue value;
    private MovementTypeEnum type;
    private Instant creationDateTime;

    public StockMovement(int id, StockValue value, MovementTypeEnum type, Instant creationDateTime) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.creationDateTime = creationDateTime;
    }

    public StockMovement(StockValue value, MovementTypeEnum type, Instant creationDateTime) {
        this.value = value;
        this.type = type;
        this.creationDateTime = creationDateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StockValue getValue() {
        return value;
    }

    public void setValue(StockValue value) {
        this.value = value;
    }

    public MovementTypeEnum getType() {
        return type;
    }

    public void setType(MovementTypeEnum type) {
        this.type = type;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", value=" + value +
                ", type=" + type +
                ", creationDateTime=" + creationDateTime +
                '}';
    }
}
