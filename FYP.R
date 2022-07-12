# Ya ALI (AS) Madad

library(dplyr)
library(stringr)
library(leaflet)
library(ggplot2)
library(scales)

data <- read.csv("Police_Department_Incident_Reports__Historical_2003_to_May_2018 (1).csv")
data <- data[sample(nrow(data),50000),]
write.csv(data, "a.csv", row.names = FALSE)
data<-read.csv("a.csv")

#summary(data)
#dim(data)
#str(data)

filtered_Data <- select(data, c(PdId, IncidntNum, Incident.Code, Category, Descript, DayOfWeek, Date, Time, PdDistrict, Resolution, Address, X, Y, location))
#View(filtered_Data)

case_Conversion <- function(x) { str_to_title(x) }

filtered_Data$Category <- case_Conversion(filtered_Data$Category)
filtered_Data$Descript <- case_Conversion(filtered_Data$Descript)
filtered_Data$Resolution <- case_Conversion(filtered_Data$Resolution)
filtered_Data$PdDistrict <- case_Conversion(filtered_Data$PdDistrict)

filtered_Data$popup <- paste("<b>Incident #: </b>", data$IncidntNum, "<br>", "<b>Category: </b>", data$Category,
                             "<br>", "<b>Description: </b>", filtered_Data$Descript,
                             "<br>", "<b>Day of week: </b>", filtered_Data$DayOfWeek,
                             "<br>", "<b>Date: </b>", filtered_Data$Date,
                             "<br>", "<b>Time: </b>", filtered_Data$Time,
                             "<br>", "<b>PD district: </b>", filtered_Data$PdDistrict,
                             "<br>", "<b>Resolution: </b>", filtered_Data$Resolution,
                             "<br>", "<b>Address: </b>", filtered_Data$Address,
                             "<br>", "<b>Longitude: </b>", filtered_Data$X,
                             "<br>", "<b>Latitude: </b>", filtered_Data$Y)

#filtered_Data <- filtered_Data[1:1000,]

leaflet(filtered_Data, width = "100%") %>% addTiles() %>%
      addTiles(group = "OSM (default)") %>%
      addProviderTiles(provider = "Esri.WorldStreetMap",group = "World StreetMap") %>%
      addProviderTiles(provider = "Esri.WorldImagery",group = "World Imagery") %>%
      # addProviderTiles(provider = "NASAGIBS.ViirsEarthAtNight2012",group = "Nighttime Imagery") %>%
      addMarkers(lng = ~X, lat = ~Y, popup = filtered_Data$popup, clusterOptions = markerClusterOptions()) %>%
      addLayersControl(baseGroups = c("OSM (default)","World StreetMap", "World Imagery"),
            options = layersControlOptions(collapsed = FALSE))



df_crime_daily <- filtered_Data %>%
   mutate(Date = as.Date(Date, "%m/%d/%Y")) %>%
   group_by(Date) %>%
   summarize(count = n()) %>%
   arrange(Date)


plot <- ggplot(df_crime_daily, aes(x = Date, y = count)) +
   geom_line(color = "#F2CA27", size = 0.1) +
   geom_smooth(color = "#1A1A1A") +
   # fte_theme() +
   scale_x_date(breaks = date_breaks("1 year"), labels = date_format("%Y")) +
   labs(x = "Date of Crime", y = "Number of Crimes", title = "Daily Crimes in San Francisco from 2007 - 2016")
plot

dev.copy(png,"a.png")
dev.off()