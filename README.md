<p align="center">
  <img src="./assets/CROSS-Logo.png" width="200" height="200" alt="CROSS Logo"/>
</p>

<h3 align="center">CROSS City Cloud</h3>
<h4 align="center">Extension, Deployment and Operation</h4>

---

<p align="center"><i>loCation pROof techniqueS for consumer mobile applicationS</i></p>

## Structure

| Directory                          |                           Description                           |
|:-----------------------------------|:---------------------------------------------------------------:|
| [CA](CA)                           |                   CROSS Certificate Authority                   |
| [CROSSCityServer](CROSSCityServer) |                  CROSS City Server V2 _(Java)_                  |
| [CROSSPlayback](CROSSPlayback)     |   CROSS Client used for Replaying Trip Dataset Files _(Java)_   |
| [MessageBroker](MessageBroker)     |       Message Broker Docker Compose _(Kafka + Zookeeper)_       |
| [Pipeline](Pipeline)               | CROSS Network Observations Integration Pipeline _(Apache Beam)_ |

## Authors

| Name              | University                 | More info                                                                                                                                                                                                                                                                                                                                                                                       |
|-------------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Lucas Vicente     | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:lucasdhvicente@gmail.com "lucasdhvicente@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/WARSKELETON "WARSKELETON") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/lucas-vicente-a91819184/ "lucas-vicente-a91819184") |
| Rafael Figueiredo | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo")                               |
| Ricardo Grade     | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:ricardo.grade@tecnico.ulisboa.pt "ricardo.grade@tecnico.ulisboa.pt") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/RicardoGrade "RicardoGrade") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/RicardoGrade "RicardoGrade")      |

## Publication

Vicente, L. H.; Eisa, S. & Pardal, M. L.  
_LoCaaS: Location-Certification-as-a-Service_  
21st IEEE International Symposium on Network Computing and Applications (NCA), 2022  

```bibtex
@InProceedings{Vicente_2022_NCA_LoCaaS,
  author    = {Lucas H. Vicente and Samih Eisa and Miguel L. Pardal},
  booktitle = {21st IEEE International Symposium on Network Computing and Applications (NCA)},
  title     = {{LoCaaS: Location-Certification-as-a-Service}},
  year      = {2022},
  month     = dec,
  abstract  = {Millions of tourists each year use smartphone applications to discover points of interest. Despite relying heavily on location sensing, most of them are susceptible to location spoofing, but not all. CROSS City is a smart tourism application that rewards users for completing tourist itineraries and uses location certificates to prevent attacks. In this case, the location verification relies on the periodic collection of public Wi-Fi network observations by multiple users to make sure the travelers actually went to the points of interest.
In this paper, we introduce the Location-Certification-as-a-Service (LoCaaS) approach, supported by a cloud-native and improved location certification system, capable of producing and validating time-bound location proofs using network data collected by tourists’ mobile devices. We show that the system can efficiently compute the stable and transient networks for a given location that are used, respectively, to validate the location of a tourist and to prove the time-of-visit. The system was deployed to the Google Cloud Platform and was validated with performance experiments and a real-world deployment.},
  keywords  = {Location Spoofing, Location Certificate, Security, Cloud Computing},
}
```
