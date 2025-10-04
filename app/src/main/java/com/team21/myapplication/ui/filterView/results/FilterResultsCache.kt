package com.team21.myapplication.ui.filterView.results

import com.team21.myapplication.ui.filterView.state.PreviewCardUi

/**
 * Cache simple para pasar resultados desde FilterRoute hasta la pantalla de resultados.
 * (Prototipo: evita parcelables/args complejos. Puedes reemplazarlo luego por VM compartido.)
 */
object FilterResultsCache {
    var items: List<PreviewCardUi> = emptyList()
}