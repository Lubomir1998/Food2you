package com.example.food2you.adapters

fun formattedStringPrice(price: String): String {
    val stotinki: String

    if(price.contains(".")) {
        val leva = price.split(".")[0]
        stotinki = price.split(".")[1]

        if(stotinki.length == 1) {
            return "${leva}.${stotinki}0"
        }
        else if(stotinki.length > 2) {
            return "${leva}.${stotinki.substring(0, 2)}"
        }
        return price
    }

    return "${price}.00"
}