package com.team21.myapplication.ui.createPostView.state

data class SuggestedPrice(
    val value: Double,          // precio recomendado (mediana)
    val low: Double,            // p25
    val high: Double,           // p75
    val compsCount: Int,        // número de comparables
    val note: String = ""       // breve justificación
)
