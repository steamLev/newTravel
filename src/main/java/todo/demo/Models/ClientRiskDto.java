package todo.demo.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRiskDto implements Serializable {
    
    private String partyId;
    private String error;
    private int attempt;
    private String data;
    private long timestamp;
    
    public ClientRiskDto(String partyId, String error, int attempt) {
        this.partyId = partyId;
        this.error = error;
        this.attempt = attempt;
        this.timestamp = System.currentTimeMillis();
    }
}