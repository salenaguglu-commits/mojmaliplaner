package com.elena.mojmaliplaner.data

import androidx.annotation.DrawableRes
import com.elena.mojmaliplaner.R

enum class StickerRarity(val labelSr: String, val labelEn: String) {
    COMMON("Česta", "Common"),
    RARE("Retka", "Rare"),
    LEGENDARY("Legendarna", "Legendary")
}

data class StickerAnimal(
    val id: Int,
    val nameSr: String,
    val nameEn: String,
    val rarity: StickerRarity,
    val triviaSr: List<String>,
    val triviaEn: List<String>,
    val emoji: String,
    val unlocksPet: Boolean = false
)

object StickerCatalog {
    val STICKERS = listOf(
        StickerAnimal(
            id = 1,
            nameSr = "Aksolotl",
            nameEn = "Axolotl",
            rarity = StickerRarity.LEGENDARY,
            triviaSr = listOf("Zovu ga i 'vodeni pas'.", "Može da obnovi svoje telo!", "Zauvek ostaje u obliku bebe."),
            triviaEn = listOf("Also known as the 'water dog'.", "Can regenerate its body parts!", "Remains in its baby form forever."),
            emoji = "🦎",
            unlocksPet = true
        ),
        StickerAnimal(
            id = 2,
            nameSr = "Kapibara",
            nameEn = "Capybara",
            rarity = StickerRarity.LEGENDARY,
            triviaSr = listOf("Najveći glodar na svetu.", "Obožava da se kupa!", "Svi ostali ljubimci je vole."),
            triviaEn = listOf("The largest rodent in the world.", "Loves to take baths!", "All other animals love them."),
            emoji = "🐹",
            unlocksPet = true
        ),
        StickerAnimal(
            id = 3,
            nameSr = "Narval",
            nameEn = "Narwhal",
            rarity = StickerRarity.RARE,
            triviaSr = listOf("Jednorog iz mora.", "Rog je zapravo dugačak zub.", "Živi u hladnom arktičkom okeanu."),
            triviaEn = listOf("The unicorn of the sea.", "The horn is actually a long tooth.", "Lives in the cold Arctic ocean."),
            emoji = "🐳",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 4,
            nameSr = "Ptica Kivi",
            nameEn = "Kiwi Bird",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Ne može da leti.", "Ima nozdrve na vrhu kljuna.", "Spava i do 20 sati dnevno!"),
            triviaEn = listOf("Cannot fly.", "Has nostrils at the tip of its beak.", "Sleeps up to 20 hours a day!"),
            emoji = "🥝",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 5,
            nameSr = "Kljunar",
            nameEn = "Platypus",
            rarity = StickerRarity.RARE,
            triviaSr = listOf("Kljun patke, rep dabra.", "Leže jaja iako je sisar!", "Svetli u mraku pod UV svetlom."),
            triviaEn = listOf("Duck bill, beaver tail.", "Lays eggs even though it's a mammal!", "Glows under UV light."),
            emoji = "🦆",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 6,
            nameSr = "Crvena Panda",
            nameEn = "Red Panda",
            rarity = StickerRarity.LEGENDARY,
            triviaSr = listOf("Ima rep dug kao i njeno telo.", "Najviše voli bambus.", "Spava na drveću."),
            triviaEn = listOf("Tail is as long as its body.", "Loves bamboo the most.", "Sleeps in trees."),
            emoji = "🐼",
            unlocksPet = true
        ),
        StickerAnimal(
            id = 7,
            nameSr = "Tarsijer",
            nameEn = "Tarsier",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Oči su mu veće od mozga.", "Može da okrene glavu za 180 stepeni.", "Ima jako duge prste."),
            triviaEn = listOf("Eyes are larger than its brain.", "Can turn its head 180 degrees.", "Has very long fingers."),
            emoji = "🐒",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 8,
            nameSr = "Okapi",
            nameEn = "Okapi",
            rarity = StickerRarity.RARE,
            triviaSr = listOf("Rođak žirafe.", "Ima noge kao zebra.", "Jezik mu je toliko dug da može njime da očisti uši!"),
            triviaEn = listOf("Cousin of the giraffe.", "Has legs like a zebra.", "Tongue so long it can clean its own ears!"),
            emoji = "🦓",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 9,
            nameSr = "Kvoka",
            nameEn = "Quokka",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Najsrećnija životinja na svetu.", "Stalno se 'smeje'.", "Živi u Australiji."),
            triviaEn = listOf("The happiest animal in the world.", "Always appears to be 'smiling'.", "Lives in Australia."),
            emoji = "🐻",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 10,
            nameSr = "Pangolin",
            nameEn = "Pangolin",
            rarity = StickerRarity.RARE,
            triviaSr = listOf("Telo mu je prekriveno krljuštima.", "Kada se uplaši, umota se u lopticu.", "Nema zube."),
            triviaEn = listOf("Body is covered in scales.", "Rolls into a ball when scared.", "Has no teeth."),
            emoji = "🦔",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 11,
            nameSr = "Pustinjska Lisica Fenek",
            nameEn = "Fennec Fox",
            rarity = StickerRarity.LEGENDARY,
            triviaSr = listOf("Najmanja lisica na svetu.", "Ima ogromne uši da bi se hladila.", "Spava preko dana u pesku."),
            triviaEn = listOf("Smallest fox in the world.", "Has huge ears to cool down.", "Sleeps in the sand during the day."),
            emoji = "🦊",
            unlocksPet = true
        ),
        StickerAnimal(
            id = 12,
            nameSr = "Lenjivac",
            nameEn = "Sloth",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Kreće se izuzetno sporo.", "Na krznu mu rastu zelene alge.", "Skoro ceo život provede viseći naopako."),
            triviaEn = listOf("Moves extremely slowly.", "Green algae grows in its fur.", "Spends almost its whole life hanging upside down."),
            emoji = "🦥",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 13,
            nameSr = "Morska Krava",
            nameEn = "Manatee",
            rarity = StickerRarity.RARE,
            triviaSr = listOf("Nežni džin iz vode.", "Jede samo morsku travu.", "Srodnik je slona!"),
            triviaEn = listOf("Gentle giant of the water.", "Eats only sea grass.", "A distant relative of the elephant!"),
            emoji = "🐄",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 14,
            nameSr = "Riba Kapljica",
            nameEn = "Blobfish",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Izgleda vrlo 'tužno'.", "Nema kosti ni mišiće.", "Živi jako duboko u okeanu gde je ogroman pritisak."),
            triviaEn = listOf("Looks very 'sad'.", "Has no bones or muscles.", "Lives very deep in the ocean under immense pressure."),
            emoji = "🫠💧",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 15,
            nameSr = "Roda Cipelokljunka",
            nameEn = "Shoebill Stork",
            rarity = StickerRarity.RARE,
            triviaSr = listOf("Kljun joj izgleda kao stara cipela.", "Veoma je visoka i može dugo da stoji potpuno nepomična.", "Ponekad zvuči kao mitraljez kada klepeće kljunom!"),
            triviaEn = listOf("Beak looks like an old shoe.", "Very tall and can stand perfectly still for a long time.", "Sometimes sounds like a machine gun when clattering its beak!"),
            emoji = "🦩",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 16,
            nameSr = "Dambo Hobotnica",
            nameEn = "Dumbo Octopus",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Ima male peraje koje podsećaju na uši slončeta Damba.", "Živi na najvećim dubinama mora.", "Nema mastilo!"),
            triviaEn = listOf("Has small fins resembling the ears of Dumbo the elephant.", "Lives at extreme ocean depths.", "Does not produce ink!"),
            emoji = "🐙",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 17,
            nameSr = "Riba Naduvnjača",
            nameEn = "Pufferfish",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Kada se uplaši, napumpa se kao balon!", "Ima bodlje koje je štite.", "Jako loše pliva."),
            triviaEn = listOf("Inflates like a balloon when scared!", "Has protective spines.", "A very clumsy swimmer."),
            emoji = "🐡",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 18,
            nameSr = "Kameleon",
            nameEn = "Chameleon",
            rarity = StickerRarity.RARE,
            triviaSr = listOf("Može da menja boju kože.", "Oči mu se kreću nezavisno jedno od drugog.", "Jezik mu je brži od metka!"),
            triviaEn = listOf("Can change its skin color.", "Eyes move independently of each other.", "Its tongue shoots out faster than a bullet!"),
            emoji = "🦎",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 19,
            nameSr = "Armadilo",
            nameEn = "Armadillo",
            rarity = StickerRarity.COMMON,
            triviaSr = listOf("Ima sopstveni oklop.", "U slučaju opasnosti smota se u savršenu loptu.", "Spava do 16 sati dnevno."),
            triviaEn = listOf("Has its own armor.", "Rolls into a perfect ball when in danger.", "Sleeps up to 16 hours a day."),
            emoji = "🦔",
            unlocksPet = false
        ),
        StickerAnimal(
            id = 20,
            nameSr = "Snežna Sova",
            nameEn = "Snowy Owl",
            rarity = StickerRarity.LEGENDARY,
            triviaSr = listOf("Ima prelepo potpuno belo perje.", "Lovi plen čak i ispod debelog snega.", "Hedviga iz Harija Potera je bila Snežna Sova!"),
            triviaEn = listOf("Has beautiful pure white feathers.", "Hunts prey even under thick snow.", "Hedwig from Harry Potter was a Snowy Owl!"),
            emoji = "🦉❄️",
            unlocksPet = true
        )
    )
}
