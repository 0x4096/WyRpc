package com.wy.rpc.common.response;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 22:17
 * @Description: RPC 请求相应处理
 */
public class RpcResponse {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 请求标志 id
     */
    private String requestId;

    /**
     * 异常信息
     */
    private Throwable error;

    /**
     * 响应结果
     */
    private Object result;

    public RpcResponse() {
        this.success = true;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
