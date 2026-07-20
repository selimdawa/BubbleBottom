package io.selimdawa.bubblebottom

typealias IBottomNavigationListener = (model: Model) -> Unit

class Model(var id: Int, var icon: Int) {
    var count: String = BubbleBottomNavigationCell.EMPTY_VALUE
}