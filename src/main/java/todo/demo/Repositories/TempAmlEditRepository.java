package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todo.demo.Models.TempAmlEdit;

import java.util.List;
import java.util.Optional;

@Repository
public interface TempAmlEditRepository extends JpaRepository<TempAmlEdit, Long> {
    
    /**
     * Находит запись по partyId
     * @param partyId идентификатор клиента
     * @return Optional с записью или пустой Optional
     */
    Optional<TempAmlEdit> findByPartyId(String partyId);
    
    /**
     * Находит все записи с определенной ошибкой
     * @param error тип ошибки
     * @return список записей
     */
    List<TempAmlEdit> findByError(String error);
    
    /**
     * Находит все записи с попыткой меньше указанной
     * @param attempt количество попыток
     * @return список записей
     */
    List<TempAmlEdit> findByAttemptLessThan(int attempt);
    
    /**
     * Проверяет существование записи по partyId
     * @param partyId идентификатор клиента
     * @return true если запись существует
     */
    boolean existsByPartyId(String partyId);
    
    /**
     * Удаляет запись по partyId
     * @param partyId идентификатор клиента
     */
    void deleteByPartyId(String partyId);
}