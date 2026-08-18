package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.dto.JuryScoreDTO;
import tr.edu.inonu.oys.model.JuryScore;

import java.util.List;

@Repository
public interface JuryScoreRepository extends JpaRepository<JuryScore, Long> {

    // Belirli bir başvuruya ait tüm puanları bul
    List<JuryScore> findByApplicationId(Long applicationId);

    // Bir jürinin belirli bir başvuruya daha önce puan verip vermediğini kontrol et
    boolean existsByJuryIdAndApplicationId(Long juryId, Long applicationId);

    // YENİ METOT: Bir başvuruya ait tüm puanları DTO olarak getir (Admin paneli için)
    @Query("SELECT new tr.edu.inonu.oys.dto.JuryScoreDTO(j.firstName, j.lastName, js.score) " +
           "FROM JuryScore js JOIN js.jury j WHERE js.application.id = :applicationId")
    List<JuryScoreDTO> findScoresByApplicationIdAsDTO(@Param("applicationId") Long applicationId);
}
