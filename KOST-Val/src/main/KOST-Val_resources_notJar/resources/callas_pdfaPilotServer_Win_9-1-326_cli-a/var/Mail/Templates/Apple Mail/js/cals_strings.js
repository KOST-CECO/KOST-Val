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
      "cc":"Cc: ",
      "bcc":"Bcc: ",
      "subject":"Subject: ",
      "_attachments":"Attachments: ",
      "_attachment_sep":"; ",
      "_attachment_sep":"; ",
      "_srcfiles":"Original eMail as sourcefile:",
      "_date_format":"DD MMM YYYY hh:mm A"
    },
  "de":
    {
      "x-envelope-to":"",
      "from":"Von: ",
      "in-reply-to":"Antwort an: ",
      "date":"Datum: ",
      "to":"An: ",
      "cc":"Kopie: ",
      "bcc":"Blindkopie: ",
      "subject":"Betreff: ",
      "_attachments":"Anlagen: ",
      "_attachment_sep":"; ",
      "_srcfiles":"Originalmail als Quelldokument:",
      "_date_format":"DD. MMMM YYYY HH:mm"
    },
  "fr":
    {
      "x-envelope-to":"",
      "from":"De: ",
      "in-reply-to":"Répondre à: ",
      "date":"Date: ",
      "to":"À: ",
      "cc":"Cc: ",
      "bcc":"CCi: ",
      "subject":"Sujet: ",
      "_attachments":"pièce-jointe: ",
      "_attachment_sep":"; ",
      "_srcfiles":"EMail origine comme fichier source:",
      "_date_format":"DD MMMM YYYY HH:mm"
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
