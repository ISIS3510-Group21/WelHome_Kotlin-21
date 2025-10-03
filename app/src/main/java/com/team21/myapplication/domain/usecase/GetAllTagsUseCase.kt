package com.team21.myapplication.domain.usecase

import com.team21.myapplication.data.model.HousingTag
import com.team21.myapplication.data.repository.HousingTagRepository

class GetAllTagsUseCase(
    private val repo: HousingTagRepository
) {
    suspend operator fun invoke(): List<HousingTag> = repo.getAllTags()
}
