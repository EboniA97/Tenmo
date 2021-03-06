package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransfer implements TransfersDao {
        private final JdbcTemplate jdbcTemplate ;

    public JdbcTransfer (DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

        @Override
        public void makeTransfer(Transfer transfer){
            String sql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
                    " VALUES (DEFAULT, ?, ?, ?, ?, ? );";
            jdbcTemplate.update(sql,transfer.getTransferId(), transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());

        }

        @Override
        public List<Transfer> getTransfersByUserId ( int userId){
            String sql = "SELECT * " +
                    "FROM transfers " +
                    "JOIN accounts ON accounts.account_id = transfers.account_from OR accounts.account_id = transfers.account_to " +
                    "WHERE user_id = ?";
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            List<Transfer> transfers = new ArrayList<>();

            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }

            return transfers;
        }


    @Override
        public Transfer getTransferByTransferId ( int transferId){
            String sql = "SELECT * " +
                    "FROM transfers WHERE transfer_id = ?";
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId);
            Transfer transfer = null;

            if (result.next()) {
                transfer = mapRowToTransfer(result);
            }

            return transfer;
        }

        @Override
        public List<Transfer> getAllTransfers () {
            String sql = "SELECT * " +
                    "FROM transfers";

            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            List<Transfer> transfers = new ArrayList<>();

            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }

            return transfers;
        }

        @Override
        public List<Transfer> getPendingTransfersByUserId ( int userId){
            String sql = "SELECT transfer_id, transfer_type_id, transfers.transfer_status_id, account_from, account_to, amount " +
                    "FROM transfers " +
                    "JOIN accounts ON accounts.account_id = transfers.account_from " +
                    "JOIN transfer_statuses ON transfers.transfer_status_id = transfer_statuses.transfer_status_id " +
                    "WHERE user_id = ? AND transfer_status_desc = 'Pending'";
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            List<Transfer> transfers = new ArrayList<>();

            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
            return transfers;
        }

        @Override
        public void updateTransfer (Transfer transfer){
            String sql = "UPDATE transfers " +
                    "SET transfer_status_id = ? " +
                    "WHERE transfer_id = ?";

            jdbcTemplate.update(sql, transfer.getTransferStatusId(), transfer.getTransferId());
        }

        private Transfer mapRowToTransfer(SqlRowSet result){
            int transferId = result.getInt("transfer_id");
            int transferTypeId = result.getInt("transfer_type_id");
            int transferStatusId = result.getInt("transfer_status_id");
            int accountFrom = result.getInt("account_from");
            int accountTo = result.getInt("account_to");
            double amountDouble = result.getDouble("amount");

            return new Transfer(transferId, transferTypeId, transferStatusId, accountFrom, accountTo,amountDouble);
        }
    }

