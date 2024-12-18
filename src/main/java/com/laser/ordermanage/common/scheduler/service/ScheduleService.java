package com.laser.ordermanage.common.scheduler.service;

import com.laser.ordermanage.common.exception.CustomCommonException;
import com.laser.ordermanage.common.exception.CommonErrorCode;
import com.laser.ordermanage.common.scheduler.dto.request.JobRequest;
import com.laser.ordermanage.common.scheduler.job.CreateIngredientStockAndPriceJob;
import com.laser.ordermanage.common.scheduler.job.type.JobType;
import com.laser.ordermanage.common.scheduler.util.JobUtil;
import com.laser.ordermanage.common.scheduler.job.ChangeStageToCompletedJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final SchedulerFactoryBean schedulerFactoryBean;

    public void createJobForChangeStageToCompleted(Long orderId) {
        JobRequest jobRequest = JobRequest.builder()
                .name(orderId.toString())
                .group(ChangeStageToCompletedJob.class.getName()) // orderId 에 해당하는 거래의 상태를 COMPLETED 로 변경하는 작업
                .jobType(JobType.ONE_TIME)
                .startAt(DateBuilder.futureDate(7, DateBuilder.IntervalUnit.DAY)) // 7일 후
                .build();
        this.addJob(jobRequest, ChangeStageToCompletedJob.class);
    }

    public void createJobForCreateIngredientStockAndPrice() {
        JobRequest jobRequest = JobRequest.builder()
                .name(CreateIngredientStockAndPriceJob.class.getName())
                .group(CreateIngredientStockAndPriceJob.class.getName())
                .jobType(JobType.MONTHLY)
                .build();
        this.addJob(jobRequest, CreateIngredientStockAndPriceJob.class);
    }

    private void addJob(JobRequest jobRequest, Class<? extends Job> jobClass) {
        Trigger trigger = JobUtil.createTrigger(jobRequest);
        JobDetail jobDetail = JobUtil.createJob(jobRequest, jobClass);

        try {
            schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new CustomCommonException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void removeJobForChangeStageToCompleted(Long orderId) {
        this.removeJob(String.valueOf(orderId), ChangeStageToCompletedJob.class.getName());
    }

    public void removeJobForCreateIngredientStockAndPrice() {
        this.removeJob(CreateIngredientStockAndPriceJob.class.getName(), CreateIngredientStockAndPriceJob.class.getName());
    }

    private void removeJob(String jobName, String groupName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);

        try {
            schedulerFactoryBean.getScheduler().deleteJob(jobKey);
        } catch (SchedulerException e) {
            // Trigger 해제를 실패하는 경우
            throw new CustomCommonException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
