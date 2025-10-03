package com.team21.myapplication.domain.usecase

import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.data.repository.HousingTagRepository

class SearchPreviewsByTagsUseCase(
    private val repo: HousingTagRepository
) {
    suspend operator fun invoke(
        selectedTagIds: List<String>,
        mode: FilterMode
    ): List<HousingPreview> = repo.getPreviewsForTags(selectedTagIds, mode)
}
