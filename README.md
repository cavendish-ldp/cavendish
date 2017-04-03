# Cavendish
## LDP on BlazeGraph
Cavendish endeavours to be both an implementation of [LDP](https://www.w3.org/TR/ldp/) and a laboratory for elaborating [Fedora 4](http://duraspace.org/about_fedora) APIs as extensions of the LDP specification. We are also interested in better understanding how the constituent frameworks of a Fedora 4 implementation influence the way its core model is understood- in this case, building a Fedora on a triplestore. Cavendish is built on [BlazeGraph](https://www.blazegraph.com/).
## Running Cavendish
```bash
cd cavendish
mvn install
cd cavendish-jetty
JETTY_HOME=file:`pwd`/src/main/webapp
BG_CONFIG=`pwd`/src/main/webapp/WEB-INF/RWStore.properties
JETTY_XML=`pwd`/src/main/resources/jetty.xml
mvn exec:java -Djetty.home=$JETTY_HOME -Dbigdata.propertyFile=$BG_CONFIG -DjettyXml=$JETTY_XML
```
## Colophon
> By this Poetical Description, you may perceive, that my ambition is not onely to be Empress, but Authoress of a whole World;
> and that the Worlds I have made, both the Blazing- and the other Philosophical World, mentioned in the first part of this Description, are framed and composed of the most pure, that is, the Rational parts of Matter, which are the parts of my Mind;
> which Creation was more easily and suddenly effected, than the Conquests of the two famous Monarchs of the World.
>
> ... as for the Blazing-world, it having an Empress already, who rules it with great Wisdom and Conduct, which Empress is my dear Platonick Friend;
> I shall never prove so unjust, treacherous and unworthy to her, as to disturb her Government, much less to depose her from her Imperial Throne, for the sake of any other, but rather chuse to create another World for another Friend.
>
> <cite>Margaret Cavendish, in the Epilogue to [The Blazing-World](http://digital.library.upenn.edu/women/newcastle/blazing/blazing.html)</cite>
