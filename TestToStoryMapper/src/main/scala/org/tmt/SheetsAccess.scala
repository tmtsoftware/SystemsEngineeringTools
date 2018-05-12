package org.tmt

import java.io.{File, IOException, InputStreamReader}

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.model._
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import scala.collection.JavaConverters._

/* https://developers.google.com/sheets/api/quickstart/java */
class SheetsAccess {

  val APPLICATION_NAME = "Sheets Access Demo"
  val JSON_FACTORY = JacksonFactory.getDefaultInstance
  val CREDENTIALS_FOLDER = "credentials"

  val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
  val spreadsheetId = "1nykcRxvKTftgEYqAjUUErluaukBaUDZ3ybQQP9z-d7A"
  val range = "Sheet1"

  /**
    * Global instance of the scopes required by this quickstart.
    * If modifying these scopes, delete your previously saved credentials/ folder.
    */
  val SCOPES = List(SheetsScopes.SPREADSHEETS).asJava
  val CLIENT_SECRET_DIR = "/client_id.json"


  /**
    * Creates an authorized Credential object.
    *
    * @param httpTransport The network HTTP Transport.
    * @return An authorized Credential object.
    * @throws IOException If there is no client_secret.
    */
  private def getCredentials(httpTransport: NetHttpTransport) = {
    val in = getClass.getResourceAsStream(CLIENT_SECRET_DIR)
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(new File(CREDENTIALS_FOLDER)))
      .setAccessType("offline")
      .build()
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
  }


  val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
    .setApplicationName(APPLICATION_NAME)
    .build()



  def getAllData() = {

    val response = service.spreadsheets().values()
      .get(spreadsheetId, range)
      .execute

    val values = response.getValues


    if (values == null || values.isEmpty) {
      println("No data found")
    } else {
      println("Class, test, Stories")
      for (javarow <- values.asScala) {
        val row = javarow.asScala
        println(s"${row(3)} | ${row(4)} | ${row(5)}")
      }
    }

    values
  }


  def clearData() = {
    val requestBody = new ClearValuesRequest()
    val result = service.spreadsheets().values().clear(spreadsheetId, range, requestBody)
      .execute()
  }

  def writeData(data: List[List[Any]]): Unit = {
    val javaData = data.map(_.map(_.asInstanceOf[AnyRef]).asJava).asJava
    val body = new ValueRange().setValues(javaData)
    val result = service.spreadsheets().values().update(spreadsheetId, range, body)
      .setValueInputOption("RAW")
      .execute()
  }
}
