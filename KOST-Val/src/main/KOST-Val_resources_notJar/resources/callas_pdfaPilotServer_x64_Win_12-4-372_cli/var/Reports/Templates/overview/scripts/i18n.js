var globalTranslations = undefined;

function readLocalTranslations() {
    try {
        const xhttp = new XMLHttpRequest();
        xhttp.open("GET", "scripts/translations.json", false);
        xhttp.send();
        const json = JSON.parse(xhttp.responseText);
        globalTranslations = json;
    }catch(err) {}
}

function translateElement(element, language)  {
    const key = element.getAttribute("translation-key");

    const localTranslation = globalTranslations && globalTranslations[language] && globalTranslations[language][key];
    const translation = localTranslation || cals_dict[key] || "<missing translation>";

    element.insertAdjacentText("afterbegin", translation);
}

function translate() {
    const language = cals_env_info.language;
    const i18nElements = document.querySelectorAll("[translation-key]");

    if(globalTranslations === undefined) {
        readLocalTranslations();
    }

    for(let i = 0; i < i18nElements.length; i++) {
        translateElement(i18nElements[i], language);
    }
}

function getTranslatedMessage(attr, messageValues) {
    const language = cals_env_info.language;

    if(globalTranslations === undefined) {
        readLocalTranslations();
    }

    const localTranslation = globalTranslations && globalTranslations[language] && globalTranslations[language][attr];
    const key = localTranslation || cals_dict[attr];

    if(key === undefined) return 'missing key "cals_dict.' + attr + '"';
    if(typeof key === "string") return key;

    if(key.msg !== undefined) {
        let parsedString = key.msg;
        let keys = Object.keys(messageValues);

        for(let i = 0; i < keys.length; i++) {
            let regex = new RegExp("@\\(" + keys[i] + "\\)", "g");
            parsedString = parsedString.replace(regex, messageValues[keys[i]]);
        }

        return parsedString;
    }
}