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
      "_date_format":"dddd, DD MMMM YYYY hh:mm A",
      "_appointment_start":"Meeting starts:",
      "_appointment_end":"Meeting ends:",
      "_appointment_date_format":"dddd, DD MMMM YYYY hh:mm A ZZ"
    },
  "de":
    {
      "x-envelope-to":"",
      "from":"Von: ",
      "in-reply-to":"Antwort an: ",
      "date":"Gesendet: ",
      "to":"An: ",
      "cc":"Cc: ",
      "bcc":"Bcc: ",
      "subject":"Betreff: ",
      "_attachments":"Anlagen: ",
      "_attachment_sep":"; ",
      "_srcfiles":"Originalmail als Quelldokument: ",
      "_date_format":"dddd, DD. MMMM YYYY HH:mm",
      "_appointment_start":"Besprechungsbeginn:",
      "_appointment_end":"Besprechungsende:",
      "_appointment_date_format":"dddd, DD. MMMM YYYY HH:mm ZZ"
    },
  "fr":
    {
      "x-envelope-to":"",
      "from":"De: ",
      "in-reply-to":"Répondre à: ",
      "date":"Date: ",
      "to":"À: ",
      "cc":"Cc: ",
      "bcc":"Cci: ",
      "subject":"Sujet: ",
      "_attachments":"pièce-jointe: ",
      "_attachment_sep":"; ",
      "_srcfiles":"##Original eMail as sourcefile: ",
      "_date_format":"dddd, DD MMMM YYYY HH:mm",
      "_appointment_start":"##Meeting starts:",
      "_appointment_end":"##Meeting ends:",
      "_appointment_date_format":"dddd, DD MMMM YYYY HH:mm ZZ"
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
