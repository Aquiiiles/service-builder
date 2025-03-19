# getH7G5EntryKey
curl http://localhost:8080/api/jsonws/ohqiwtsfhl.h7g5entry/get-h7-g5-entry-key \
-u test@liferay.com:learn \
-d key='rerere'

# getMyCustomH7G5FolderId
curl http://localhost:8080/api/jsonws/ohqiwtsfhl.h7g5entry/get-my-custom-h7-g5-folder-id \
-u test@liferay.com:learn \
-d h7g5FolderId=4857894

# getH7G5EntryName
curl http://localhost:8080/api/jsonws/ohqiwtsfhl.h7g5entry/get-h7-g5-entry-name \
-u test@liferay.com:learn \
-d name='Jesus%20'

# getH7G5ByHDN
curl http://localhost:8080/api/jsonws/ohqiwtsfhl.h7g5entry/get-h7-g5-by-hdn \
-u test@liferay.com:learn \
-d h7g5folderId=43894389 \
-d description='Jesus%20' \
-d name='loves%20you'