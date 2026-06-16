package com.campuslearning.server.repository;

import com.campuslearning.server.model.QuestionBankItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionBankItemRepository extends JpaRepository<QuestionBankItem, Long> {

    List<QuestionBankItem> findByPresetKeyOrderBySortOrderAsc(String presetKey);

    List<QuestionBankItem> findAllByOrderByPresetKeyAscSortOrderAsc();
}
