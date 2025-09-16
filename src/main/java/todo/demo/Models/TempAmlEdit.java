package todo.demo.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempAmlEdit implements Serializable {
    
    private String partyId;
    private String error;
    private int attempt;
    private String data;
    private long timestamp;
    
    public static TempAmlEdit buildTempAmlEdit(ClientRiskDto clientRiskDto) {
        return TempAmlEdit.builder()
                .partyId(clientRiskDto.getPartyId())
                .error(clientRiskDto.getError())
                .attempt(clientRiskDto.getAttempt())
                .data(clientRiskDto.getData())
                .timestamp(clientRiskDto.getTimestamp())
                .build();
    }
}