DELETE FROM config_data
WHERE param_name IN ('GPB_BANK_URL', 'GPB_BANK_PORTAL_ID', 'GPB_MERCHANT_ID',
'BACK_URL_S','BACK_URL_F','paymentCheckURL','hostName','GpbBankSBPURL',
'GPBSBPMerchantId');