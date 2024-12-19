// Localized strings:

var cals_strings =
{
  "en":
    {
      "x-envelope-to":"",
      "from":"From: ",
      "in-reply-to":"Reply to: ",
      "date":"Date: ",
      "to":"To: ",
      "cc":"CC: ",
      "bcc":"BCC: ",
      "subject":"Subject: ",
      "_attachments":"Attachments: ",
      "_attachment_sep":"; ",
      "_attachment_sep":"; ",
      "_srcfiles":"Original eMail as sourcefile: ",
      "_date_format":"DD.MM.YYYY hh:mm A"
    },
  "de":
    {
      "x-envelope-to":"",
      "from":"Von: ",
      "in-reply-to":"Antwort an: ",
      "date":"Datum: ",
      "to":"An: ",
      "cc":"Kopie (CC): ",
      "bcc":"Blindkopie (BCC): ",
      "subject":"Betreff: ",
      "_attachments":"Anlagen: ",
      "_attachment_sep":"; ",
      "_srcfiles":"Originalmail als Quelldokument: ",
      "_date_format":"DD.MM.YYYY HH:mm"
    },
  "fr":
    {
      "x-envelope-to":"",
      "from":"De: ",
      "in-reply-to":"Répondre à: ",
      "date":"Date: ",
      "to":"Pour: ",
      "cc":"Copie à: ",
      "bcc":"Copie cachée à: ",
      "subject":"Sujet: ",
      "_attachments":"pièce jointe: ",
      "_attachment_sep":"; ",
      "_srcfiles":"EMail origine comme fichier source: ",
      "_date_format":"DD.MM.YYYY HH:mm"
    }
};

function cals_strings_initLanguage()
{
	if( !cals_strings[cals_ccmip_mapping.language] )
	{
		cals_ccmip_mapping.language = "en";
	}
}

cals_strings_initLanguage()
