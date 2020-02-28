#Painikepelin käyttöliittymä

Repositoryssä on Painikepeli.apk joka on valmis asennettavaksi android laitteelle.
Vaihtoehtoisesti mobiilisovellusta voi testata esimerkiksi android emulaattorilla.
Android studion emulaattori on ilmainen ja sen voi ladata ilmaiseksi netistä.
sovelluksen saa asennettua kyseiseen emulaattoriin seuraavasti:
1. lataa Painikepeli.apk koneelle
2. avaa emulaattori android studiossa
3. valitse Painikepeli.apk latauskansiossa ja liu'uta se emulaattorin päälle(drag&drop)
4. sovellus on nyt asennettu ja sitä voi testata emulaattorissa
5. jos haluat testata sovellusta asettamalla nimen uudestaan tulee asetuksissa poistaa
pelin tallennustiedot jolloin peli pyyhkii sovellukseen asettamasi nimen ja voit asettaa nimen uudestaan.


Sovellus sisältää useampia tiedostoja jotka löytyvät repositorystä seuraavista poluista:

/main/java/com/example/painikepeli

./MainActivity.java käsittelee pelaajan painallukset ja asettaa mobiilisovelluksen graafisen käyttöliittymän tulostamaan pelaajalle pelaajan pisteet ja muut pelin tiedot. pitää myös huolen että pelitiedot tallennetaan puhelimen muistiin. luokka on muuten rakennettu johdonmukaisesti paitsi handlerin osalta joka on jouduttu määrittelemään alussa vaikka se ajetaan vasta viimeisenä toimintona painiketta painaessa.

./setNameActivity.java käsittelee pelaajan nimen lisäystä joka ei suoranaisesti liity pelin vaadittuihin ominaisuuksiin mutta on tärkeää pelin kannalta jotta tietokannassa saadaan eroteltua pelaajat joka tässä tapauksessa tapahtuu nimen perusteella. Myös tämä luokka on mielestäni muuten johdon mukaisesti rakennettu lukuunottamatta handler luokkaa joka on jouduttu määrittelemään luokan alussa vaikka se ajetaan aina vasta nimen määrittelyn loppuvaiheessa.

./serversocket.java käsittelee rajapintaa joka lähettää pyyntöjä palvelimelle ja palauttaa vastaukset luokkien handlereille

Koodi on mielestäni helpointa käydä läpi seuraavassa järjestyksessä
(toteutus tapahtui myös tässä järjestyksessä)

-lue läpi luokka MainActivity mutta skippaa handler
-lue läpi luokka setNameActivity mutta skippaa handler
-lue läpi serversocket luokka
-lue läpi handlerit

/main/res/

sisältää graaffisia komponentteja
./layout kansiosta löytää näkymät sovelluksille
./values kansiosta löytää muotoiluja joita käytetään esimerkiksi painikkeiden tyyleissä ja teksteissä

mobiilisovelluksen toteutus ei niinkään keskity käyttöliittymän ulkonäköön vaan toimivuuteen. Joten jätin mobiilisovelluksen käyttöliittymän yksinkertaisen näköiseksi. Ohje painikkeen ei ole määritelty tekevän mitään.

Yhteenveto lopputuloksesta:

Painikepeli toimii kuten pitääkin omien testausten osalta. Virhetilanne voi sattua nimeä lisättäessä tai painiketta painaessa jos palvelin vastaa liian hitaasti. Heroku ei ilmeisesti pidä sovelluksia jatkuvasti täysin aktiivisesti käynnissä vaan odottaa kunnes sovellukseen otetaan yhteyttä. Tämä johti muutaman kerran connection timeout erroriin serversocket luokassa. Pidensin timeoutin aikaa kyseisestä syystä.




