package dmi.developments.skin_disease_cam.activities

object RemediesActivity {

    fun getRemediesFor(diseaseName: String): String {
        return when (diseaseName) {

            "Tinea (Fungal) Infection" -> """
                <b>Clotrimazole Cream or Ointment</b><br/><br/>
                <b>Instructions:</b><br/><br/>
                1. Clean and dry the infected area thoroughly.<br/><br/>
                2. Apply a small amount of <b>Clotrimazole cream</b> twice daily (morning and night).<br/><br/>
                3. Continue for <b>2–4 weeks</b>, even if symptoms disappear early, to prevent recurrence.<br/><br/>
                    ⚠️ Avoid contact with eyes or mucous membranes.
            """.trimIndent()

            "Atopic Dermatitis (Eczema)" -> """
                <b>Guaviderm (Guava Extract) – by DOST</b><br/><br/>
                <b>Instructions:</b><br/><br/>
                1. Wash the affected area with mild soap and water.<br/><br/>
                2. Pat dry gently with a clean towel.<br/><br/>
                3. Apply a thin layer of <b>Guaviderm cream</b> to the affected skin <b>2–3 times daily</b>.<br/><br/>
                4. Continue use until symptoms improve or as advised by a healthcare provider.<br/><br/>
                    💡Guava extract has natural antibacterial and anti-inflammatory properties.
            """.trimIndent()

            "Warts (Verruca Vulgaris)" -> """
                <b>Salicylic Acid (20–40%) + Duct Tape + Sandpaper</b><br/><br/>
                <b>Instructions (Dr. Melissa Piliang, 2020):</b><br/><br/>
                1. Apply salicylic acid to the wart.<br/><br/>
                2. Cover completely with <b>duct tape</b>, keeping it sealed <b>24 hours a day</b>.<br/><br/>
                3. If the tape falls off, replace it immediately.<br/><br/>
                4. After <b>2–3 weeks</b>, if the wart looks smaller, continue until it disappears.<br/><br/>
                5. When the wart becomes <b>white and soft</b>, gently rub it with <b>sandpaper</b> to remove the outer layer.<br/><br/>
                6. Replace duct tape and repeat every few days or weekly.<br/><br/>
                    🧴 Dispose of used sandpaper after each use.
            """.trimIndent()

            "Impetigo" -> """
                
                <b>Guaviderm (Guava Extract) – by DOST</b><br/><br/>
                <b>Instructions:</b><br/><br/>
                1. Wash the affected area with mild soap and water.<br/><br/>
                2. Pat dry gently with a clean towel.<br/><br/>
                3. Apply a thin layer of <b>Guaviderm cream</b> to the affected skin <b>2–3 times daily</b>.<br/><br/>
                4. Continue use until symptoms improve or as advised by a healthcare provider.<br/><br/>
                    💡Guava extract has natural antibacterial and anti-inflammatory properties.
                
            """.trimIndent()


            "Sarcoptes Scabiei (Scabies)" -> """
                <b>Guaviderm (Guava Extract) – by DOST</b><br/><br/>
                <b>Instructions:</b><br/><br/>
                1. Wash the affected area with mild soap and water.<br/><br/>
                2. Pat dry gently with a clean towel.<br/><br/>
                3. Apply a thin layer of <b>Guaviderm cream</b> to the affected skin <b>2–3 times daily</b>.<br/><br/>
                4. Continue use until symptoms improve or as advised by a healthcare provider.<br/><br/>
                💡Guava extract has natural antibacterial and anti-inflammatory properties.
            """.trimIndent()

            "Prickly Heat (Miliaria)" -> """
                <b>Cold Shower + Calamine Lotion + Moisturizer</b><br/><br/>
                <b>Instructions:</b><br/><br/>
                1. Take a cool shower to soothe and clean the skin.<br/><br/>
                2. Gently pat dry — avoid rubbing.<br/><br/>
                3. Apply Calamine lotion to relieve itching and irritation.<br/><br/>
                4. Once absorbed, apply a gentle moisturizer to prevent dryness.<br/><br/>
                    💧 Avoid tight clothing and stay in a cool environment.
            """.trimIndent()

            else -> "No remedies available for this condition."
        }
    }
}
