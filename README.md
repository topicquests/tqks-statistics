# tqks-statistics
A statistics collector for the TopicQuests Knowledge System

## Usage
The utility is to be used as a _singleton_ such that any platform in the same JVM can share it.<br/>
The concept is this:<br/>
* Each platform (app) on a JVM calls _StatisticsUtility.getInstance()_.
* Each platform, say, a TopicMap, has items it wishes to count; in the case of a TopicMap, a count of new topics makes sense.
* Each platform therefore invents its own _Key_ types; a TopicMap would use the key "NumTopics".
* Each platform grabs a copy of the utility with _getInstance()_; the first to call that will construct it and load any statistics saved from before.
* Any platform on the JVM can call _saveData()_; the first to do so actually causes data to be saved; others have no effect.
* However, if one app calls _saveData()_ and another one continues to use it, the internal flag is reset, and _saveData()_ will be available. If the stats are updated after saving, and not saved again, that added data will be lost.
