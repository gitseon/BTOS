package com.umc.btos.src.history;

import com.umc.btos.config.Constant;
import com.umc.btos.src.history.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class HistoryDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // ===================================  History 목록 조회 & 발신인 조회 ===================================

    // 일기 & 편지 & 답장 발신인 닉네임 목록 반환 (createdAt 기준 내림차순 정렬)
    public List<String> getNickNameList_sortedByCreatedAt(int userIdx) {
        String query = "SELECT DISTINCT senderNickName " +
                "FROM ( " +
                "SELECT User.nickName AS senderNickName, Diary.createdAt AS sendAt " +
                "FROM User " +
                "INNER JOIN (DiarySendList INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx) " +
                "ON User.userIdx = Diary.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "UNION " +
                "SELECT User.nickName AS senderNickName, Letter.createdAt AS sendAt " +
                "FROM User " +
                "INNER JOIN (LetterSendList INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx) " +
                "ON User.userIdx = Letter.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "UNION " +
                "SELECT User.nickName AS senderNickName, Reply.createdAt As sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "ORDER BY sendAt DESC " +
                ") senderNickName";

        return this.jdbcTemplate.queryForList(query, String.class, userIdx, userIdx, userIdx);
    }

    // --------------------------------------- null 확인 ---------------------------------------

    // 일기 null 확인 : filtering == sender
    public int hasHistory_diary(int userIdx, String senderNickName) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "AND User.nickName = ? " +
                "AND DiarySendList.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 편지 null 확인 : filtering == sender
    public int hasHistory_letter(int userIdx, String senderNickName) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "AND User.nickName = ? " +
                "AND LetterSendList.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 답장 null 확인 : filtering == sender
    public int hasHistory_reply(int userIdx, String senderNickName) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "AND User.nickName = ? " +
                "AND Reply.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 일기 null 확인 : filtering == diary
    public int hasHistory_diary(int userIdx) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "AND DiarySendList.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 null 확인 : filtering == letter
    public int hasHistory_letter(int userIdx) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "AND LetterSendList.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 답장 null 확인 : filtering == letter
    public int hasHistory_reply(int userIdx) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "AND Reply.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- List<History_Sender> size ---------------------------------------
    // filtering == sender && search == null

    // 일기 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public int getDiaryListSize(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public int getLetterListSize(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 답장 (Reply.receiverIdx = userIdx AND User.nickName = senderNickName)
    public int getReplyListSize(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND User.nickName = ? AND Reply.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // --------------------------------------- List<History> ---------------------------------------
    // filtering == diary || letter (paging)

    // 일기 (DiarySendList.receiverIdx = userIdx)
    public History getDiary_done(int userIdx, int diaryIdx) {
        String query = "SELECT Diary.diaryIdx AS typeIdx, User.nickName AS senderNickName, " +
                "Diary.content AS content, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, " +
                "DiarySendList.createdAt AS sendAt_raw, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND Diary.diaryIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getInt("doneListNum"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, diaryIdx);
    }

    // 일기 (DiarySendList.receiverIdx = userIdx)
    public History getDiary_nonDone(int userIdx, int diaryIdx) {
        String query = "SELECT Diary.diaryIdx AS typeIdx, User.nickName AS senderNickName, " +
                "Diary.content AS content, Diary.emotionIdx AS emotionIdx, " +
                "DiarySendList.createdAt AS sendAt_raw, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND Diary.diaryIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        0,
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, diaryIdx);
    }

    // 일기 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public History getDiary_done(int userIdx, String senderNickName) {
        String query = "SELECT Diary.diaryIdx AS typeIdx, " +
                "Diary.content AS content, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, " +
                "DiarySendList.createdAt AS sendAt_raw, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        senderNickName,
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getInt("doneListNum"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 일기 (DiarySendList.receiverIdx = userIdx)
    public History getDiary_nonDone(int userIdx, String senderNickName) {
        String query = "SELECT Diary.diaryIdx AS typeIdx, User.nickName AS senderNickName, " +
                "Diary.content AS content, Diary.emotionIdx AS emotionIdx, " +
                "DiarySendList.createdAt AS sendAt_raw, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        0,
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx)
    public List<History> getLetterList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT Letter.letterIdx AS typeIdx, User.nickName AS senderNickName, Letter.content AS content, " +
                "LetterSendList.createdAt AS sendAt_raw, date_format(LetterSendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, startData, endData);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public List<History> getLetterList(int userIdx, String senderNickName) {
        String query = "SELECT Letter.letterIdx AS typeIdx, Letter.content AS content, " +
                "LetterSendList.createdAt AS sendAt_raw, date_format(LetterSendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("typeIdx"),
                        senderNickName,
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 답장 (Reply.receiverIdx = userIdx)
    public List<History> getReplyList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT Reply.replyIdx AS typeIdx, User.nickName AS senderNickName, Reply.content AS content, " +
                "Reply.createdAt AS sendAt_raw, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND Reply.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, startData, endData);
    }

    // 답장 (Reply.receiverIdx = userIdx AND User.nickName = senderNickName)
    public List<History> getReplyList(int userIdx, String senderNickName) {
        String query = "SELECT Reply.replyIdx AS typeIdx, User.nickName AS senderNickName, Reply.content AS content, " +
                "Reply.createdAt AS sendAt_raw, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND User.nickName = ? AND Reply.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "reply",
                        rs.getInt("typeIdx"),
                        senderNickName,
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // --------------------------------------- List<History> size ---------------------------------------

    // 일기 (filtering = diary)
    public int getDiaryList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM DiarySendList WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 (filtering = letter)
    public int getLetterList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM LetterSendList WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 답장 (filtering = letter)
    public int getReplyList_dataNum(int userIdx) {

        String query = "SELECT COUNT(*) FROM Reply WHERE Reply.receiverIdx = ? AND Reply.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- History ---------------------------------------

    // 일기
    public History getDiary(int userIdx, int diaryIdx) {
        String query = "SELECT Diary.diaryIdx AS typeIdx, User.nickName AS senderNickName," +
                "Diary.content AS content, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, " +
                "DiarySendList.createdAt AS sendAt_raw, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND Diary.diaryIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getInt("doneListNum"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, diaryIdx);
    }

    // 일기 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public History getDiary(int userIdx, String senderNickName) {
        String query = "SELECT Diary.diaryIdx AS typeIdx, " +
                "Diary.content AS content, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, " +
                "DiarySendList.createdAt AS sendAt_raw, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC " + // 발신일 기준 내림차순 정렬
                "LIMIT 1"; // 상위 첫번째 값

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        senderNickName,
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getInt("doneListNum"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 편지
    public History getLetter(int userIdx, int letterIdx) {
        String query = "SELECT Letter.letterIdx AS typeIdx, User.nickName AS senderNickName, Letter.content AS content, " +
                "LetterSendList.createdAt AS sendAt_raw, date_format(LetterSendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND Letter.letterIdx = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, letterIdx);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public History getLetter(int userIdx, String senderNickName) {
        String query = "SELECT Letter.letterIdx AS typeIdx, Letter.content AS content, " +
                "LetterSendList.createdAt AS sendAt_raw, date_format(LetterSendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("typeIdx"),
                        senderNickName,
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 답장
    public History getReply(int userIdx, int replyIdx) {
        String query = "SELECT Reply.replyIdx AS typeIdx, User.nickName AS senderNickName, Reply.content AS content, " +
                "Reply.createdAt AS sendAt_raw, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND Reply.replyIdx = ? AND Reply.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, replyIdx);
    }

    // 답장 (Reply.receiverIdx = userIdx AND User.nickName = senderNickName)
    public History getReply(int userIdx, String senderNickName) {
        String query = "SELECT Reply.replyIdx AS typeIdx, User.nickName AS senderNickName, Reply.content AS content, " +
                "Reply.createdAt AS sendAt_raw, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND User.nickName = ? AND Reply.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "reply",
                        rs.getInt("typeIdx"),
                        senderNickName,
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }


    // --------------------------------------- idxList ---------------------------------------
    // search != null

    // diaryIdx 리스트 반환 : filtering = sender
    public List<Integer> getDiaryIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, DiarySendList.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // letterIdx 리스트 반환 : filtering = sender
    public List<Integer> getLetterIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, LetterSendList.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // replyIdx 리스트 반환 : filtering = sender
    public List<Integer> getReplyIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Reply.replyIdx AS idx, Reply.createdAt AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND User.nickName = ? AND Reply.status = 'active' " +
                "ORDER BY sendAt DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // diaryIdx 리스트 반환 : filtering = diary
    public List<Integer> getDiaryIdxList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, DiarySendList.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, startData, endData);
    }

    // letterIdx 리스트 반환 : filtering = letter
    public List<Integer> getLetterIdxList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, LetterSendList.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, startData, endData);
    }

    // replyIdx 리스트 반환 : filtering = letter
    public List<Integer> getReplyIdxList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT idx FROM (" +
                "SELECT Reply.replyIdx AS idx, Reply.createdAt AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND Reply.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, startData, endData);
    }

    // --------------------------------------- idxList size ---------------------------------------

    // diaryIdx 리스트 반환 시 (filtering = diary) data 개수 반환
    public int getDiaryIdxList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM DiarySendList WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // letterIdx 리스트 반환 시 (filtering = letter) data 개수 반환
    public int getLetterIdxList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM LetterSendList WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // letterIdx 리스트 반환 시 (filtering = letter) data 개수 반환
    public int getReplyIdxList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM Reply WHERE Reply.receiverIdx = ? AND Reply.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- idx ---------------------------------------
    // filtering = sender && search != null

    // diaryIdx (createAt 기준 내림차순 정렬 시 상위 1번째 항목)
    public int getDiaryIdx_sender(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, DiarySendList.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // letterIdx (createAt 기준 내림차순 정렬 시 상위 1번째 항목)
    public int getLetterIdx_sender(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, LetterSendList.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // replyIdx (createAt 기준 내림차순 정렬 시 상위 1번째 항목)
    public int getReplyIdx_sender(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Reply.replyIdx AS idx, Reply.createdAt AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND User.nickName = ? AND Reply.status = 'active' " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // --------------------------------------- content ---------------------------------------

    // Diary.content 반환
    public String getDiaryContent(int diaryIdx) {
        String query = "SELECT content FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
    }

    // Letter.content 반환
    public String getLetterContent(int letterIdx) {
        String query = "SELECT content FROM Letter WHERE letterIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, letterIdx);
    }

    // Reply.content 반환
    public String getReplyContent(int replyIdx) {
        String query = "SELECT content FROM Reply WHERE replyIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, replyIdx);
    }

    // ===================================  History 본문 조회 ===================================

    // --------------------------------------- 본문 ---------------------------------------

    // 일기
    public GetHistoryRes_Main getDiary_main(int diaryIdx) {
        String query = "SELECT Diary.diaryIdx AS typeIdx, Diary.content, Diary.emotionIdx, User.nickName AS senderNickName, " +
                "DiarySendList.createdAt AS sendAt_raw, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE Diary.diaryIdx = ? AND DiarySendList.status = 'active' " +
                "GROUP BY Diary.diaryIdx";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetHistoryRes_Main(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), diaryIdx);
    }

    // 일기 done list
    public List<Done> getDoneList_main(int diaryIdx) {
        String query = "SELECT Done.doneIdx, Done.content " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE Diary.diaryIdx = ? AND DiarySendList.status = 'active' " +
                "GROUP BY Done.doneIdx";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Done(
                        rs.getInt("doneIdx"),
                        rs.getString("content")
                ), diaryIdx);
    }

    // 해당 일기 done list 유무 반환
    public int hasDone(int diaryIdx) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM Done " +
                "WHERE diaryIdx = ? " +
                "AND status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 편지
    public GetHistoryRes_Main getLetter_main(int letterIdx) {
        String query = "SELECT Letter.letterIdx AS typeIdx, Letter.content, User.nickName AS senderNickName, " +
                "LetterSendList.createdAt AS sendAt_raw, date_format(LetterSendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE Letter.letterIdx = ? " +
                "AND LetterSendList.status = 'active' " +
                "GROUP BY Letter.letterIdx";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetHistoryRes_Main(
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), letterIdx);
    }

    // --------------------------------------- List<Reply> ---------------------------------------

    // 일기
    public List<GetHistoryRes_Main> getReplyList_diary(int userIdx, int diaryIdx) {
        String query = "SELECT Reply.replyIdx AS typeIdx, Reply.content, User.nickName AS senderNickName, " +
                "Reply.createdAt AS sendAt_raw, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'diary' " +
                "AND DiarySendList.sendIdx = " +
                "(SELECT DISTINCT DiarySendList.sendIdx FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "AND Diary.diaryIdx = ?)";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes_Main(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, userIdx, diaryIdx);
    }

    // 편지
    public List<GetHistoryRes_Main> getReplyList_letter(int userIdx, int letterIdx) {
        String query = "SELECT Reply.replyIdx AS typeIdx, Reply.content, User.nickName AS senderNickName, " +
                "Reply.createdAt AS sendAt_raw, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'letter' " +
                "AND LetterSendList.sendIdx = " +
                "(SELECT DISTINCT LetterSendList.sendIdx " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "AND Letter.letterIdx = ?)";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes_Main(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt")
                ), userIdx, userIdx, letterIdx);
    }

    // --------------------------------------- idx ---------------------------------------

    // diaryIdx
    public int getDiaryIdx_main(int replyIdx) {
        String query = "SELECT DISTINCT Diary.diaryIdx " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = DiarySendList.diaryIdx " +
                "WHERE Reply.sendIdx = (SELECT sendIdx FROM Reply WHERE replyIdx = ?)";

        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
    }

    // letterIdx
    public int getLetterIdx_main(int replyIdx) {
        String query = "SELECT DISTINCT Letter.letterIdx " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.diaryIdx = Letter.letterIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = LetterSendList.letterIdx " +
                "WHERE Reply.sendIdx = (SELECT sendIdx FROM Reply WHERE replyIdx = ?)";

        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
    }

    // --------------------------------------- firstHistoryType ---------------------------------------

    public String getHistoryType(int replyIdx) {
        String query = "SELECT firstHistoryType FROM Reply WHERE Reply.replyIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, replyIdx);
    }

}
