description = "A tool designed to detect business Logic flaws."

zapAddOn {
    addOnName.set("cherrybomb")
    zapVersion.set("2.11.1")

    manifest {
        author.set("Nathan Sitbon")
    }
}

crowdin {
    configuration {
        val resourcesPath = "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/"
        tokens.put("%messagesPath%", resourcesPath)
        tokens.put("%helpPath%", resourcesPath)
    }
}
