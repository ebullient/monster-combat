Understanding application behavior is important in any environment. The most efficient way to observe application behavior relies on metrics, key/value pairs of numerical data. This session will compare the capabilities of libraries like Micrometer, OpenTelemetry, and MicroProfile metrics. We'll also explore how gathered data can be used to observe and understand application behavior in order to determine what should be measured.

dbourne 8:03 AM
hey erin, do you mean screenshots of Liberty grafana dashboard?
erin 8:05 AM
No. Json. And wtf I do w/ Prometheus query language. i have a talk on Tuesday that will fix the hands-on gap in my brain (which is exactly why I submitted it in the first place)
So. Your favorite queries and dashboards would be awesome
I am not using Liberty.. but I would appreciate something to replicate
Probably checked in somewhere?
But I still want Don’s best list..
dbourne 8:06 AM
oh, ok, so you're trying to figure out what real prom queries look like... one sec
from recent stuff, the dashboard for open liberty operator is at https://github.com/OpenLiberty/open-liberty-operator/blob/master/deploy/dashboards/metrics/ibm-websphere-liberty-grafana-dashboard-metrics-2.0.json
deploy/dashboards/metrics/ibm-websphere-liberty-grafana-dashboard-metrics-2.0.json
```
{
 "__inputs": [
    {
      "name": "DS_PROMETHEUS",
 Show more
<https://github.com/OpenLiberty/open-liberty-operator|OpenLiberty/open-liberty-operator>OpenLiberty/open-liberty-operator | Added by a bot
that one uses mpMetrics-2.0, which i mention because in 2.0 we have labels on the metrics which makes the queries simpler/better
dbourne 8:12 AM
prior to that our metric names contained parts that were dynamic -- eg. servlet names embedded in the metric name itself.  That leads you to having to do less natural things with prom QL, as you can see in the other dashboad in that same directory (basically you end up using regular expressions in your prom QL queries to do what should be simple stuff)


6 replies
Last reply today at 9:02 AMView thread
dbourne 8:18 AM
one that I like, that is a work in progress...
Liberty-Metrics-M2-G5-20190521-test2-1570364242581.json
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
Click to expand inline (2,212 lines)


in that one, we're revising the view of servlet count and servlet response time.  Have a look at line 738 and 829...
738: sum(vendor_servlet_request_total{pod=~`[[pod]]`}) by (servlet)
829: avg(rate(vendor_servlet_responseTime_total_seconds{pod=~`[[pod]]`}[[dur]]) / rate(vendor_servlet_request_total{pod=~`[[pod]]`}[[dur]])) by (servlet)
the first one is saying - for each servlet show the sum of servlet hits across the list of pods provided
the second is more interesting - for each servlet show the average of the rate of change of the number of seconds spent serving the servlet (over the most recent dur) divided by the rate of change of the number of requests to that servlet (over the most recent dur) for the list of pods provided
:100:
1

is that helping?
dbourne 8:36 AM
and I assume you've seen this... which is a good place to start - https://prometheus.io/docs/prometheus/latest/querying/basics/
prometheus.ioprometheus.io
Querying basics | Prometheus
An open-source monitoring system with a dimensional data model, flexible query language, efficient time series database and modern alerting approach.
erin 8:42 AM
Broke for a shower. Reading
I read latter link.. but it is blahblahblah for my brain right now.
I have not cracked the Rosetta Stone to understand what it is trying to tell me
dbourne 8:46 AM
hm.  how about I break down my most complex query in way you can understand?
ground up
you kind of need to see the results on a dashboard as you learn... that helps
erin 8:47 AM
Have you looked at prom metrics that spring produces? They are almost matching prom metric labels... but not quite. There is a filter to make them match
dbourne 8:48 AM
nope
erin 8:49 AM
Ok. I have to stare at those more
I did something bananas
:banana:
1

dbourne 8:49 AM
what?
erin 8:50 AM
https://github.com/ebullient/monster-combat

ebullient/monster-combat
Language
Java
Last updated
10 hours ago
<https://github.com/ebullient/monster-combat|ebullient/monster-combat>ebullient/monster-combat | Oct 4th | Added by a bot
dbourne 8:50 AM
omg, interested... :slightly_smiling_face:
erin 8:51 AM
It should self build. Can just run without Kube
Root url will link to endpoints including prom one so you can see what it spits out. (There are more modules I haven’t picked up the emit even more ... but I don’t know the real value of tracking GCs in prom?)
Do JVM stats matter that much anymore?
dbourne 8:54 AM
cpu and mem info can be helpful if you are wondering about why things are scaling or slow
eg. what % of available cpus is my pod using up?
erin 8:57 AM
But... Kube can already see that?
You don’t need any app-level metrics for that
dbourne 8:58 AM
true, but if you are building a perf dashboard you should probably show the cpu consumption of your pod - whether it comes from kub or from microprofile or ...
and you won't always be running in kub
erin 8:59 AM
I know.. because my app wasn’t getting scraped for awhile.. I had to figure out the thing about ServiceMonitor and that it only wants some endpoint paths, and Spring puts that endpoint elsewhere.
and the path line doesn’t work. So! I sorted that with nefarious tricks that I am totally including in this talk. ;) (edited)
dbourne 9:01 AM
yup, the new ServiceMonitor stuff makes things more complicated for initial setup
erin 9:02 AM
I like it better, actually
I don’t like messing w/ global config all the time
this is better isolation.. just fubar for Spring
until path attribute is picked up
but I hacked it. ;) I win!
dbourne 9:03 AM
:slightly_smiling_face: it was nice in the past though to be able to just add an annotation to your deployment and be done
now you have to set up a separate service monitor, which is well isolated and avoids the deployment needing to have been pre-set up for prom scraping, but is still extra
and glad you got it going for spring :wink:
erin 9:25 AM
messing with global config always seemed super weird to me
it kind of meant that every group (every LOB, every whatever) would have to set up and configure their own prom instance.
dbourne 9:45 AM
uhm, where are the monsters in your dnd monster combat?
erin 9:46 AM
Oh!! One sec. I generate it
erin 9:55 AM
Crap, I can’t pull the generated file from my computer at home via my ipad. Something wrong w/ login. Will have to sort later.
let me get you generator thingie.. it’s another git repo..
https://github.com/ebullient/DnDAppFiles

ebullient/DnDAppFiles
Files for the Fifth Edition apps by Lion's Den
Last updated
6 days ago
Forks
362
<https://github.com/ebullient/DnDAppFiles|ebullient/DnDAppFiles>ebullient/DnDAppFiles | Sep 29th | Added by a bot
requires python..
erin 10:11 AM
Lemme try again
erin 10:24 AM
I win the internet
Downloading file to ipad
Will slack in a moment
I have a fake one for testing.. but that is no fun
dbourne 10:25 AM
:red_trophy:  ??
(sorry, that seems sarcastic, not meant to be)
erin 10:26 AM
I downloaded file from home to my ipad via sftp
And am now maybe sharing with slack? It is doing something
dbourne 10:29 AM
wow, ipad wizardry :slightly_smiling_face:
erin 10:29 AM
I just can’t tell how fast it is going. No progress bar. :)
I have to say “I win!” A lot. A) it is in the title of my talk, and B) the talk is Wednesday.. I need to keep panic at bay..
Trying again differently
Serious wizardry now
Port forwarding to do a remote screenshare to my linux box
dbourne 10:35 AM
what's your talk about?  beating up promql with spring?
erin 10:37 AM
https://springoneplatform.io/2019/sessions/metrics-for-the-win-using-micrometer-to-understand-application-behavior

springoneplatform.iospringoneplatform.io
SpringOne Platform | October 7–10, 2019 | Austin, TX
SpringOne Platform is the premier conference for getting hands-on with modern software. Meet other developers, operators, and leaders and build scalable applications people love.(61 kB)
https://d1ophd2rlqbanb.cloudfront.net/2019/S1P2019-OG.png
attempt x to transfer. remote screen share is bust. So it isn’t really about promql.. (edited)
but it is about trying to show interesting things. which means a) wtf are usual interesting things that infra-monitoring has now stolen away, and then b) how do you find more interesting QOS/app-centric/related-to-business things to look at instead?
Monster bashing is .. a useful way to generate requests of varying size (sometimes 0 rounds, sometimes 15.. )
Slack is showing me this file.. but I don’t think you can see it yet.
One thing I know I can show is when exceptions happen (and what kind). This is shiny new, so there aren’t false positives. e.g. I can tell I still have some parse errors lurking.. staring at log files all day would have made those hard to spot.
XML
BeastiaryCompendium
2 MB XML — Click to download


Then I wanted to add custom metrics (w/ micrometer) to tag/label how many rounds. How many monsters were picked, what size they are (to see if I should adjust an algorithm, say.. so that the battles are more balanced)
ooh! it transferred! woot!
ok. have to go get kiddos.
dbourne 10:46 AM
i've seen the prom people talk about having metrics for your logs.  which seems a bit weird to me since the question you immediately have is - oh crap, i have exception x i want to see that in context.  but I guess knowing that you have log entry X is helpful to begin with
if you can tie this in purely to your app-specific bits, like showing how quickly beasts are being destroyed, grouped by beast... could be pretty fun talk
erin 11:12 AM
But.. i have at least 20 charts to make today.. + figuring out this prom query stuff + understanding what people already think is interesting. i have my opinions
dbourne 11:19 AM
k, let me know if/when i can help you with learning promql... i'm not super-guru, but i know enough to help having done the journey from not getting what the words mean on the query basics page to being able to make useful dashboards
and as usual, i see you are riding the edge between crazy and panic.  :stuck_out_tongue:
erin 11:35 AM
It is an thing, for sure
dbourne 11:37 AM
i'm going to be heading out in a few min (shopping, and going to see TFC game with kristine).  if you want me to look at your charts or have questions or think of some other way i can help you, let me know (but help will most likely come tomorrow)
erin 11:37 AM
So, given the premise of this app.. what would you think about? Histograms? Guages? Counters?
dbourne 11:38 AM
is the app to actually play some dnd?  i haven't seen it in motion
erin 11:40 AM
No. Makes monsters fight to death. Rolls dice, uses stats, etc
dbourne 11:41 AM
histograms just show distribution of things... so could use that to show distribution of how many rounds different monsters are surviving?  or distribution of how many rounds it takes for monster X to defeat monster Y?
erin 11:42 AM
Not smart enough to do motion / range or extra knock-on effects
Ooh!! Good thoughts
dbourne 11:43 AM
assuming coutners/gauges behave as in dw or microprofile metrics, you would typically use gauges to show the current value of something that can go up or down (non-monotonic)
and counters only for things that change monotonically
erin 11:43 AM
Monster type etc would be labels?
dbourne 11:44 AM
yes.  that would make sense - avoid putting them into the metric name
counter - battle_wins{ monster1="Aboleth", monster2="Abominable Yeti" } - number of times monster1 beat monster2 (edited)
you'd really want to know, what is chance of monster X beating each of the other monster types
erin 11:48 AM
Ok. Gauge for battle length (number of rounds) and number of participants? Two separate ones?
dbourne 11:48 AM
for that you could have wins/losses in a histogram, with the values being positive representing number of rounds to a win, or negative representing number of rounds to a loss
erin 11:49 AM
Faceoff is always two.. so that works
Ok! This is very helpful. Thank you (edited)
dbourne 11:51 AM
you could have a histogram for how long completed battles took, with a label for how many particpants
erin 11:51 AM
Oh! Ok. That would go between faceoff and melee, then
dbourne 11:52 AM
gauge for battle length is a bit weird because you're going to have to continually create new gauges... which suggests you're going to end up with endless creation of new time series, which isn't good
erin 11:52 AM
Yeah. That works. Faceoffs take a long time
I went to 3 in one of my tests and hit an infinite loop. Woops. :)
dbourne 11:53 AM
you could have a gauge for number of battles currently in progress
a counter for number of battles completed
erin 11:53 AM
I think I smashed that bug
dbourne 11:53 AM
a counter for number of battles won... with a label for monsterName
erin 11:53 AM
Yeah!! I can do that. Woop woop!!
dbourne 11:53 AM
and of course counter for number of battles lost... with same label for monsterName...
erin 11:54 AM
Number of battles completed would cover that
dbourne 11:54 AM
or combine them battleOutcome{monster="name", outcome="win"}
erin 11:54 AM
Can use label for victor. Right
Oh!! wait! That is per monster, you mean
dbourne 11:54 AM
right, you're better to have battleOutcome (as mentioned) and then use sum(...) to add up all of the battles finished
yes, per monster
but then you can use sum to add up across all monsters if you like
erin 11:55 AM
Right. Then you can find monster stats
dbourne 11:55 AM
and you can graph all monsters by just leaving out the monster label in your prom query
erin 11:55 AM
Or group by battle id. :)
dbourne 11:56 AM
if you're going to do anything by battle id you are going to have a lot of time series... need to decide if that's giong to be too many for "enterprise production"
erin 11:57 AM
Group by a label makes more time series?
dbourne 11:57 AM
your small finite things make the best label values -- think monster names, outcomes, number of rounds to completion, fight style (melee vs. faceoff)
:100:
1

erin 11:57 AM
Ok
dbourne 11:58 AM
every different permutation makes a new time series.  if label A has 10 possible values and label B has 1000 possible values then you will have 10,000 time series to track that metric
erin 11:58 AM
Right.. I guess I thought a label didn’t create a new time series in that way..
Oh!
dbourne 11:58 AM
think of gauges and counters as things that are sampled every few seconds and stored in a database
erin 11:58 AM
And you are assuming there won’t be an infinite number of servlets, which is why you use that one

dbourne 11:59 AM
so if I want to ask what the value of someMetric is, I can see what that value was over time, for each and every permutation of the metric labels
yes
erin 11:59 AM
You are the bestest brain ever
dbourne 12:00 PM
number of servlets is reasonably small... same with number of connection pools, datasources, etc.   so that's why we got away with that for mpMetrics-1.1... but still better to do it with labels because it works way better with promql
:+1:
1

erin 12:00 PM
Ok. So they have something else I don’t understand.. (i will have more q’s when I have tried these things)..
dbourne 12:00 PM
ok, i need to scoot.  and btw, this is really fun :slightly_smiling_face:
erin 12:00 PM
Vector matching. Wtf is that
Go scoot.
If you can come back later, that’s awesome
dbourne 12:01 PM
will ping you later if I can... but will likely be 7pm or so

dbourne 8:03 AM
hey erin, do you mean screenshots of Liberty grafana dashboard?
erin 8:05 AM
No. Json. And wtf I do w/ Prometheus query language. i have a talk on Tuesday that will fix the hands-on gap in my brain (which is exactly why I submitted it in the first place)
So. Your favorite queries and dashboards would be awesome
I am not using Liberty.. but I would appreciate something to replicate
Probably checked in somewhere?
But I still want Don’s best list..
dbourne 8:06 AM
oh, ok, so you're trying to figure out what real prom queries look like... one sec
from recent stuff, the dashboard for open liberty operator is at https://github.com/OpenLiberty/open-liberty-operator/blob/master/deploy/dashboards/metrics/ibm-websphere-liberty-grafana-dashboard-metrics-2.0.json
deploy/dashboards/metrics/ibm-websphere-liberty-grafana-dashboard-metrics-2.0.json
```
{
 "__inputs": [
    {
      "name": "DS_PROMETHEUS",
 Show more
<https://github.com/OpenLiberty/open-liberty-operator|OpenLiberty/open-liberty-operator>OpenLiberty/open-liberty-operator | Added by a bot
that one uses mpMetrics-2.0, which i mention because in 2.0 we have labels on the metrics which makes the queries simpler/better
dbourne 8:12 AM
prior to that our metric names contained parts that were dynamic -- eg. servlet names embedded in the metric name itself.  That leads you to having to do less natural things with prom QL, as you can see in the other dashboad in that same directory (basically you end up using regular expressions in your prom QL queries to do what should be simple stuff)


6 replies
Last reply today at 9:02 AMView thread
dbourne 8:18 AM
one that I like, that is a work in progress...
Liberty-Metrics-M2-G5-20190521-test2-1570364242581.json
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
Click to expand inline (2,212 lines)


in that one, we're revising the view of servlet count and servlet response time.  Have a look at line 738 and 829...
738: sum(vendor_servlet_request_total{pod=~`[[pod]]`}) by (servlet)
829: avg(rate(vendor_servlet_responseTime_total_seconds{pod=~`[[pod]]`}[[dur]]) / rate(vendor_servlet_request_total{pod=~`[[pod]]`}[[dur]])) by (servlet)
the first one is saying - for each servlet show the sum of servlet hits across the list of pods provided
the second is more interesting - for each servlet show the average of the rate of change of the number of seconds spent serving the servlet (over the most recent dur) divided by the rate of change of the number of requests to that servlet (over the most recent dur) for the list of pods provided
:100:
1

is that helping?
dbourne 8:36 AM
and I assume you've seen this... which is a good place to start - https://prometheus.io/docs/prometheus/latest/querying/basics/
prometheus.ioprometheus.io
Querying basics | Prometheus
An open-source monitoring system with a dimensional data model, flexible query language, efficient time series database and modern alerting approach.
erin 8:42 AM
Broke for a shower. Reading
I read latter link.. but it is blahblahblah for my brain right now.
I have not cracked the Rosetta Stone to understand what it is trying to tell me
dbourne 8:46 AM
hm.  how about I break down my most complex query in way you can understand?
ground up
you kind of need to see the results on a dashboard as you learn... that helps
erin 8:47 AM
Have you looked at prom metrics that spring produces? They are almost matching prom metric labels... but not quite. There is a filter to make them match
dbourne 8:48 AM
nope
erin 8:49 AM
Ok. I have to stare at those more
I did something bananas
:banana:
1

dbourne 8:49 AM
what?
erin 8:50 AM
https://github.com/ebullient/monster-combat

ebullient/monster-combat
Language
Java
Last updated
10 hours ago
<https://github.com/ebullient/monster-combat|ebullient/monster-combat>ebullient/monster-combat | Oct 4th | Added by a bot
dbourne 8:50 AM
omg, interested... :slightly_smiling_face:
erin 8:51 AM
It should self build. Can just run without Kube
Root url will link to endpoints including prom one so you can see what it spits out. (There are more modules I haven’t picked up the emit even more ... but I don’t know the real value of tracking GCs in prom?)
Do JVM stats matter that much anymore?
dbourne 8:54 AM
cpu and mem info can be helpful if you are wondering about why things are scaling or slow
eg. what % of available cpus is my pod using up?
erin 8:57 AM
But... Kube can already see that?
You don’t need any app-level metrics for that
dbourne 8:58 AM
true, but if you are building a perf dashboard you should probably show the cpu consumption of your pod - whether it comes from kub or from microprofile or ...
and you won't always be running in kub
erin 8:59 AM
I know.. because my app wasn’t getting scraped for awhile.. I had to figure out the thing about ServiceMonitor and that it only wants some endpoint paths, and Spring puts that endpoint elsewhere.
and the path line doesn’t work. So! I sorted that with nefarious tricks that I am totally including in this talk. ;) (edited)
dbourne 9:01 AM
yup, the new ServiceMonitor stuff makes things more complicated for initial setup
erin 9:02 AM
I like it better, actually
I don’t like messing w/ global config all the time
this is better isolation.. just fubar for Spring
until path attribute is picked up
but I hacked it. ;) I win!
dbourne 9:03 AM
:slightly_smiling_face: it was nice in the past though to be able to just add an annotation to your deployment and be done
now you have to set up a separate service monitor, which is well isolated and avoids the deployment needing to have been pre-set up for prom scraping, but is still extra
and glad you got it going for spring :wink:
erin 9:25 AM
messing with global config always seemed super weird to me
it kind of meant that every group (every LOB, every whatever) would have to set up and configure their own prom instance.
dbourne 9:45 AM
uhm, where are the monsters in your dnd monster combat?
erin 9:46 AM
Oh!! One sec. I generate it
erin 9:55 AM
Crap, I can’t pull the generated file from my computer at home via my ipad. Something wrong w/ login. Will have to sort later.
let me get you generator thingie.. it’s another git repo..
https://github.com/ebullient/DnDAppFiles

ebullient/DnDAppFiles
Files for the Fifth Edition apps by Lion's Den
Last updated
6 days ago
Forks
362
<https://github.com/ebullient/DnDAppFiles|ebullient/DnDAppFiles>ebullient/DnDAppFiles | Sep 29th | Added by a bot
requires python..
erin 10:11 AM
Lemme try again
erin 10:24 AM
I win the internet
Downloading file to ipad
Will slack in a moment
I have a fake one for testing.. but that is no fun
dbourne 10:25 AM
:red_trophy:  ??
(sorry, that seems sarcastic, not meant to be)
erin 10:26 AM
I downloaded file from home to my ipad via sftp
And am now maybe sharing with slack? It is doing something
dbourne 10:29 AM
wow, ipad wizardry :slightly_smiling_face:
erin 10:29 AM
I just can’t tell how fast it is going. No progress bar. :)
I have to say “I win!” A lot. A) it is in the title of my talk, and B) the talk is Wednesday.. I need to keep panic at bay..
Trying again differently
Serious wizardry now
Port forwarding to do a remote screenshare to my linux box
dbourne 10:35 AM
what's your talk about?  beating up promql with spring?
erin 10:37 AM
https://springoneplatform.io/2019/sessions/metrics-for-the-win-using-micrometer-to-understand-application-behavior

springoneplatform.iospringoneplatform.io
SpringOne Platform | October 7–10, 2019 | Austin, TX
SpringOne Platform is the premier conference for getting hands-on with modern software. Meet other developers, operators, and leaders and build scalable applications people love.(61 kB)
https://d1ophd2rlqbanb.cloudfront.net/2019/S1P2019-OG.png
attempt x to transfer. remote screen share is bust. So it isn’t really about promql.. (edited)
but it is about trying to show interesting things. which means a) wtf are usual interesting things that infra-monitoring has now stolen away, and then b) how do you find more interesting QOS/app-centric/related-to-business things to look at instead?
Monster bashing is .. a useful way to generate requests of varying size (sometimes 0 rounds, sometimes 15.. )
Slack is showing me this file.. but I don’t think you can see it yet.
One thing I know I can show is when exceptions happen (and what kind). This is shiny new, so there aren’t false positives. e.g. I can tell I still have some parse errors lurking.. staring at log files all day would have made those hard to spot.
XML
BeastiaryCompendium
2 MB XML — Click to download


Then I wanted to add custom metrics (w/ micrometer) to tag/label how many rounds. How many monsters were picked, what size they are (to see if I should adjust an algorithm, say.. so that the battles are more balanced)
ooh! it transferred! woot!
ok. have to go get kiddos.
dbourne 10:46 AM
i've seen the prom people talk about having metrics for your logs.  which seems a bit weird to me since the question you immediately have is - oh crap, i have exception x i want to see that in context.  but I guess knowing that you have log entry X is helpful to begin with
if you can tie this in purely to your app-specific bits, like showing how quickly beasts are being destroyed, grouped by beast... could be pretty fun talk
erin 11:12 AM
But.. i have at least 20 charts to make today.. + figuring out this prom query stuff + understanding what people already think is interesting. i have my opinions
dbourne 11:19 AM
k, let me know if/when i can help you with learning promql... i'm not super-guru, but i know enough to help having done the journey from not getting what the words mean on the query basics page to being able to make useful dashboards
and as usual, i see you are riding the edge between crazy and panic.  :stuck_out_tongue:
erin 11:35 AM
It is an thing, for sure
dbourne 11:37 AM
i'm going to be heading out in a few min (shopping, and going to see TFC game with kristine).  if you want me to look at your charts or have questions or think of some other way i can help you, let me know (but help will most likely come tomorrow)
erin 11:37 AM
So, given the premise of this app.. what would you think about? Histograms? Guages? Counters?
dbourne 11:38 AM
is the app to actually play some dnd?  i haven't seen it in motion
erin 11:40 AM
No. Makes monsters fight to death. Rolls dice, uses stats, etc
dbourne 11:41 AM
histograms just show distribution of things... so could use that to show distribution of how many rounds different monsters are surviving?  or distribution of how many rounds it takes for monster X to defeat monster Y?
erin 11:42 AM
Not smart enough to do motion / range or extra knock-on effects
Ooh!! Good thoughts
dbourne 11:43 AM
assuming coutners/gauges behave as in dw or microprofile metrics, you would typically use gauges to show the current value of something that can go up or down (non-monotonic)
and counters only for things that change monotonically
erin 11:43 AM
Monster type etc would be labels?
dbourne 11:44 AM
yes.  that would make sense - avoid putting them into the metric name
counter - battle_wins{ monster1="Aboleth", monster2="Abominable Yeti" } - number of times monster1 beat monster2 (edited)
you'd really want to know, what is chance of monster X beating each of the other monster types
erin 11:48 AM
Ok. Gauge for battle length (number of rounds) and number of participants? Two separate ones?
dbourne 11:48 AM
for that you could have wins/losses in a histogram, with the values being positive representing number of rounds to a win, or negative representing number of rounds to a loss
erin 11:49 AM
Faceoff is always two.. so that works
Ok! This is very helpful. Thank you (edited)
dbourne 11:51 AM
you could have a histogram for how long completed battles took, with a label for how many particpants
erin 11:51 AM
Oh! Ok. That would go between faceoff and melee, then
dbourne 11:52 AM
gauge for battle length is a bit weird because you're going to have to continually create new gauges... which suggests you're going to end up with endless creation of new time series, which isn't good
erin 11:52 AM
Yeah. That works. Faceoffs take a long time
I went to 3 in one of my tests and hit an infinite loop. Woops. :)
dbourne 11:53 AM
you could have a gauge for number of battles currently in progress
a counter for number of battles completed
erin 11:53 AM
I think I smashed that bug
dbourne 11:53 AM
a counter for number of battles won... with a label for monsterName
erin 11:53 AM
Yeah!! I can do that. Woop woop!!
dbourne 11:53 AM
and of course counter for number of battles lost... with same label for monsterName...
erin 11:54 AM
Number of battles completed would cover that
dbourne 11:54 AM
or combine them battleOutcome{monster="name", outcome="win"}
erin 11:54 AM
Can use label for victor. Right
Oh!! wait! That is per monster, you mean
dbourne 11:54 AM
right, you're better to have battleOutcome (as mentioned) and then use sum(...) to add up all of the battles finished
yes, per monster
but then you can use sum to add up across all monsters if you like
erin 11:55 AM
Right. Then you can find monster stats
dbourne 11:55 AM
and you can graph all monsters by just leaving out the monster label in your prom query
erin 11:55 AM
Or group by battle id. :)
dbourne 11:56 AM
if you're going to do anything by battle id you are going to have a lot of time series... need to decide if that's giong to be too many for "enterprise production"
erin 11:57 AM
Group by a label makes more time series?
dbourne 11:57 AM
your small finite things make the best label values -- think monster names, outcomes, number of rounds to completion, fight style (melee vs. faceoff)
:100:
1

erin 11:57 AM
Ok
dbourne 11:58 AM
every different permutation makes a new time series.  if label A has 10 possible values and label B has 1000 possible values then you will have 10,000 time series to track that metric
erin 11:58 AM
Right.. I guess I thought a label didn’t create a new time series in that way..
Oh!
dbourne 11:58 AM
think of gauges and counters as things that are sampled every few seconds and stored in a database
erin 11:58 AM
And you are assuming there won’t be an infinite number of servlets, which is why you use that one

dbourne 11:59 AM
so if I want to ask what the value of someMetric is, I can see what that value was over time, for each and every permutation of the metric labels
yes
erin 11:59 AM
You are the bestest brain ever
dbourne 12:00 PM
number of servlets is reasonably small... same with number of connection pools, datasources, etc.   so that's why we got away with that for mpMetrics-1.1... but still better to do it with labels because it works way better with promql
:+1:
1

erin 12:00 PM
Ok. So they have something else I don’t understand.. (i will have more q’s when I have tried these things)..
dbourne 12:00 PM
ok, i need to scoot.  and btw, this is really fun :slightly_smiling_face:
erin 12:00 PM
Vector matching. Wtf is that
Go scoot.
If you can come back later, that’s awesome
dbourne 12:01 PM
will ping you later if I can... but will likely be 7pm or so

